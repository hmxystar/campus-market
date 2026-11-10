package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.OrderStatus;
import com.campus.market.common.ProductStatus;
import com.campus.market.dto.OrderCreateDTO;
import com.campus.market.entity.Order;
import com.campus.market.entity.Product;
import com.campus.market.mapper.OrderMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.service.OrderService;
import com.campus.market.util.RedisLock;
import com.campus.market.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单模块：并发与一致性的主战场。
 * 下单五步见讲义 8.1：加锁 → 校验商品 → 原子扣库存 → 插订单 → 释放锁。
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final Duration PAY_TIMEOUT = Duration.ofMinutes(30);

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final RedisLock redisLock;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(OrderCreateDTO dto, Long buyerId) {
        Long productId = dto.getProductId();
        String lockKey = "product:lock:" + productId;
        String token = redisLock.tryLock(lockKey, PAY_TIMEOUT);
        if (token == null) throw new BusinessException("手慢了，该商品正在被别人下单");

        try {
            Product p = productMapper.selectById(productId);
            if (p == null) throw new BusinessException(404, "商品不存在");
            if (p.getStatus() == null || p.getStatus() != ProductStatus.ON_SALE) {
                throw new BusinessException("商品已下架或已售出");
            }
            if (p.getUserId().equals(buyerId)) throw new BusinessException("不能购买自己发布的商品");

            // 原子扣库存：影响行数 0 说明被别人抢先
            int rows = productMapper.deductStock(productId);
            if (rows == 0) throw new BusinessException("商品已被抢先下单");

            Order o = new Order();
            o.setOrderNo(generateOrderNo());
            o.setBuyerId(buyerId);
            o.setSellerId(p.getUserId());
            o.setProductId(productId);
            o.setProductTitle(p.getTitle());     // 冗余快照
            o.setProductCover(p.getCover());
            o.setAmount(p.getPrice());
            o.setStatus(OrderStatus.WAIT_PAY);
            o.setExpireTime(LocalDateTime.now().plus(PAY_TIMEOUT));
            orderMapper.insert(o);               // uk_product_buyer 兜底防重复下单
            return o.getId();
        } finally {
            redisLock.unlock(lockKey, token);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderNo, Long buyerId) {
        Order o = mustGetMine(orderNo, buyerId, "buyerId");
        if (o.getStatus() != OrderStatus.WAIT_PAY) throw new BusinessException("订单状态不允许支付");
        if (o.getExpireTime() != null && o.getExpireTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("订单已超时，请重新下单");
        }
        orderMapper.update(null, Wrappers.<Order>lambdaUpdate()
                .set(Order::getStatus, OrderStatus.WAIT_SEND)
                .set(Order::getPayTime, LocalDateTime.now())
                .eq(Order::getId, o.getId())
                .eq(Order::getStatus, OrderStatus.WAIT_PAY));   // CAS，防与超时任务撞车
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(String orderNo, Long userId) {
        Order o = orderMapper.selectOne(Wrappers.<Order>lambdaQuery().eq(Order::getOrderNo, orderNo));
        if (o == null) throw new BusinessException(404, "订单不存在");
        if (!o.getBuyerId().equals(userId) && !o.getSellerId().equals(userId)) {
            throw new BusinessException(403, "无权操作该订单");
        }
        if (o.getStatus() != OrderStatus.WAIT_PAY) throw new BusinessException("只有待付款订单可以取消");

        int rows = orderMapper.update(null, Wrappers.<Order>lambdaUpdate()
                .set(Order::getStatus, OrderStatus.CANCELED)
                .set(Order::getCancelTime, LocalDateTime.now())
                .eq(Order::getId, o.getId())
                .eq(Order::getStatus, OrderStatus.WAIT_PAY));
        if (rows > 0) productMapper.restoreStock(o.getProductId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void finish(String orderNo, Long buyerId) {
        Order o = mustGetMine(orderNo, buyerId, "buyerId");
        if (o.getStatus() != OrderStatus.WAIT_SEND) throw new BusinessException("只有待发货订单可以确认完成");
        orderMapper.update(null, Wrappers.<Order>lambdaUpdate()
                .set(Order::getStatus, OrderStatus.FINISHED)
                .set(Order::getFinishTime, LocalDateTime.now())
                .eq(Order::getId, o.getId())
                .eq(Order::getStatus, OrderStatus.WAIT_SEND));
    }

    @Override
    public IPage<OrderVO> pageMine(int pageNo, int size, Integer status, Long userId) {
        Page<Order> page = new Page<>(pageNo, Math.min(size, 50));
        IPage<Order> result = orderMapper.selectPage(page,
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getBuyerId, userId)
                        .eq(status != null, Order::getStatus, status)
                        .orderByDesc(Order::getCreateTime));
        return result.convert(OrderVO::of);
    }

    private Order mustGetMine(String orderNo, Long userId, String ownerField) {
        Order o = orderMapper.selectOne(Wrappers.<Order>lambdaQuery().eq(Order::getOrderNo, orderNo));
        if (o == null) throw new BusinessException(404, "订单不存在");
        Long ownerId = "buyerId".equals(ownerField) ? o.getBuyerId() : o.getSellerId();
        if (!ownerId.equals(userId)) throw new BusinessException(403, "无权操作该订单");
        return o;
    }

    /** 订单号：时间戳 + 4 位随机数，靠数据库唯一索引兜底 */
    private String generateOrderNo() {
        return LocalDateTime.now().format(NO_FMT) + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
