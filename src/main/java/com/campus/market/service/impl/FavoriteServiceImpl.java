package com.campus.market.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.market.common.BusinessException;
import com.campus.market.entity.Favorite;
import com.campus.market.entity.Product;
import com.campus.market.mapper.FavoriteMapper;
import com.campus.market.mapper.ProductMapper;
import com.campus.market.service.FavoriteService;
import com.campus.market.vo.ProductCardVO;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;
    private final ProductMapper productMapper;

    @Override
    public void add(Long productId, Long userId) {
        Product p = productMapper.selectById(productId);
        if (p == null) throw new BusinessException(404, "商品不存在");
        try {
            Favorite f = new Favorite();
            f.setUserId(userId);
            f.setProductId(productId);
            favoriteMapper.insert(f);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("已经收藏过了");
        }
    }

    @Override
    public void cancel(Long productId, Long userId) {
        favoriteMapper.delete(Wrappers.<Favorite>lambdaQuery()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getProductId, productId));
    }

    @Override
    public IPage<ProductCardVO> pageMine(int pageNo, int size, Long userId) {
        Page<Favorite> page = new Page<>(pageNo, Math.min(size, 50));
        IPage<Favorite> favPage = favoriteMapper.selectPage(page,
                Wrappers.<Favorite>lambdaQuery()
                        .eq(Favorite::getUserId, userId)
                        .orderByDesc(Favorite::getCreateTime));

        List<Long> ids = favPage.getRecords().stream().map(Favorite::getProductId).toList();
        // TODO 第 6 章：这里可以先批量查商品再拼装，避免 N+1；当前只返回空分页骨架
        Page<ProductCardVO> voPage = new Page<>(favPage.getCurrent(), favPage.getSize(), favPage.getTotal());
        if (ids.isEmpty()) return voPage;

        List<Product> products = productMapper.selectByIds(ids);
        voPage.setRecords(products.stream().map(p -> {
            ProductCardVO vo = new ProductCardVO();
            vo.setId(p.getId());
            vo.setTitle(p.getTitle());
            vo.setPrice(p.getPrice());
            vo.setCover(p.getCover());
            vo.setViewCount(p.getViewCount());
            vo.setCreateTime(p.getCreateTime());
            return vo;
        }).toList());
        return voPage;
    }
}
