package com.campus.market.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 列表卡片：只带前端列表要展示的字段，别把整张商品表吐出去 */
@Data
public class ProductCardVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String cover;
    private Integer viewCount;
    private LocalDateTime createTime;
    private String sellerName;
}
