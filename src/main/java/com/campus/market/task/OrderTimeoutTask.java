package com.campus.market.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.market.common.OrderStatus;
import com.campus.market.entity.Order;
import com.campus.market.mapper.OrderMapper;
import com.campus.market.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 超时未付款订单自动取消。
 * 多实例部署时每个实例都会扫表 —— 上线前需加分布式锁或改成只让一个实例跑（第 10.3 节）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    /** 每分钟扫一次超时未付款订单 */
    @Scheduled(cron = "0 */1 * * * ?")
    public void cancelTimeoutOrders() {
        List<Order> list = orderMapper.selectList(
                Wrappers.<Order>lambdaQuery()
                        .eq(Order::getStatus, OrderStatus.WAIT_PAY)
                        .lt(Order::getExpireTime, LocalDateTime.now())
                        .last("LIMIT 100"));

        for (Order o : list) {
            // CAS 更新，防止和用户手动取消、支付撞车
            int rows = orderMapper.update(null,
                    Wrappers.<Order>lambdaUpdate()
                            .set(Order::getStatus, OrderStatus.CANCELED)
                            .set(Order::getCancelTime, LocalDateTime.now())
                            .eq(Order::getId, o.getId())
                            .eq(Order::getStatus, OrderStatus.WAIT_PAY));
            if (rows > 0) {
                productMapper.restoreStock(o.getProductId());
                log.info("超时订单已取消: {}", o.getOrderNo());
            }
        }
    }
}
