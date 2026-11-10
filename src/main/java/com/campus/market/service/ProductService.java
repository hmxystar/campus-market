package com.campus.market.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.dto.ProductCreateDTO;
import com.campus.market.vo.ProductCardVO;
import com.campus.market.vo.ProductDetailVO;

public interface ProductService {

    Long publish(ProductCreateDTO dto, Long userId);

    IPage<ProductCardVO> page(int pageNo, int size, Long categoryId, String keyword, String sort);

    ProductDetailVO detail(Long id);

    void update(Long id, ProductCreateDTO dto, Long userId);

    void offShelf(Long id, Long userId);
}
