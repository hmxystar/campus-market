package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("product")
public class Product extends BaseEntity {

    private Long userId;
    private Long categoryId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String cover;
    /** 逗号分隔的图片地址 */
    private String images;
    private Integer stock;
    /** 见 ProductStatus */
    private Integer status;
    private Integer viewCount;
    private Integer favoriteCount;
}
