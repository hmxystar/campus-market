package com.campus.market.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.vo.ProductCardVO;

public interface FavoriteService {

    void add(Long productId, Long userId);

    void cancel(Long productId, Long userId);

    IPage<ProductCardVO> pageMine(int pageNo, int size, Long userId);
}
