package com.campus.market.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.campus.market.entity.Product;
import com.campus.market.vo.ProductCardVO;
import org.apache.ibatis.annotations.Param;

public interface ProductMapper extends BaseMapper<Product> {

    /** 列表要「商品 + 卖家昵称」联查，单表 API 不够，写 XML */
    IPage<ProductCardVO> selectCardPage(IPage<ProductCardVO> page,
                                        @Param("categoryId") Long categoryId,
                                        @Param("keyword") String keyword,
                                        @Param("sort") String sort);

    /** 原子扣库存：防超卖的关键 SQL */
    int deductStock(@Param("id") Long id);

    /** 原子回补（取消订单） */
    int restoreStock(@Param("id") Long id);
}
