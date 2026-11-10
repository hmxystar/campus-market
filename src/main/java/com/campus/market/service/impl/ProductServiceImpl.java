package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.common.ProductStatus;
import com.campus.market.dto.ProductCreateDTO;
import com.campus.market.entity.Product;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.service.ProductService;
import com.campus.market.util.RedisUtil;
import com.campus.market.vo.ProductCardVO;
import com.campus.market.vo.ProductDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

/**
 * 商品模块：发布 / 列表 / 详情（Cache Aside）/ 编辑 / 下架。
 * 详情缓存键：product:detail:{id}，与浏览计数键 product:view:{id} 分开。
 */
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private static final String DETAIL_KEY = "product:detail:";
    private static final Duration DETAIL_TTL = Duration.ofMinutes(30);

    private final ProductMapper productMapper;
    private final RedisUtil redisUtil;

    @Override
    public Long publish(ProductCreateDTO dto, Long userId) {
        Product p = new Product();
        p.setUserId(userId);
        p.setCategoryId(dto.getCategoryId());
        p.setTitle(dto.getTitle());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setOriginalPrice(dto.getOriginalPrice());
        p.setCover(dto.getCover());
        p.setImages(dto.getImages());
        p.setStock(dto.getStock() == null ? 1 : dto.getStock());
        p.setStatus(ProductStatus.PENDING);     // 发布后先待审核
        p.setViewCount(0);
        p.setFavoriteCount(0);
        productMapper.insert(p);
        return p.getId();
    }

    @Override
    public IPage<ProductCardVO> page(int pageNo, int size, Long categoryId, String keyword, String sort) {
        Page<ProductCardVO> page = new Page<>(pageNo, Math.min(size, 50));
        return productMapper.selectCardPage(page, categoryId, keyword, sort);
    }

    /** Cache Aside：先读缓存，未命中查库回填；改库后删缓存（不是改缓存） */
    @Override
    public ProductDetailVO detail(Long id) {
        Object cached = redisUtil.get(DETAIL_KEY + id);
        if (cached instanceof ProductDetailVO vo) {
            return vo;
        }

        Product p = productMapper.selectById(id);
        if (p == null) throw new BusinessException(404, "商品不存在或已下架");

        ProductDetailVO vo = ProductDetailVO.of(p);
        redisUtil.set(DETAIL_KEY + id, vo, DETAIL_TTL);

        // 浏览数只加 Redis 计数器，由定时任务批量落库（第 10.4 节）
        redisUtil.increment("product:view:" + id, 1);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, ProductCreateDTO dto, Long userId) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BusinessException(404, "商品不存在");
        if (!p.getUserId().equals(userId)) throw new BusinessException(403, "只能修改自己发布的商品");

        p.setCategoryId(dto.getCategoryId());
        p.setTitle(dto.getTitle());
        p.setDescription(dto.getDescription());
        p.setPrice(dto.getPrice());
        p.setOriginalPrice(dto.getOriginalPrice());
        p.setImages(dto.getImages());
        if (dto.getCover() != null) p.setCover(dto.getCover());
        productMapper.updateById(p);
        redisUtil.delete(DETAIL_KEY + id);
    }

    @Override
    public void offShelf(Long id, Long userId) {
        Product p = productMapper.selectById(id);
        if (p == null) throw new BusinessException(404, "商品不存在");
        if (!p.getUserId().equals(userId)) throw new BusinessException(403, "只能下架自己发布的商品");
        if (p.getStatus() != ProductStatus.ON_SALE) throw new BusinessException("只有在售商品可以下架");

        productMapper.update(null, Wrappers.<Product>lambdaUpdate()
                .set(Product::getStatus, ProductStatus.OFF_SHELF)
                .eq(Product::getId, id));
        redisUtil.delete(DETAIL_KEY + id);
    }
}
