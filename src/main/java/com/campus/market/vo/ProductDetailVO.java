package com.campus.market.vo;

import com.campus.market.entity.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Data
public class ProductDetailVO {

    private Long id;
    private Long sellerId;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private List<String> images;
    private Integer status;
    private String statusText;
    private String sellerName;
    private LocalDateTime createTime;

    public static ProductDetailVO of(Product p) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setId(p.getId());
        vo.setSellerId(p.getUserId());
        vo.setTitle(p.getTitle());
        vo.setDescription(p.getDescription());
        vo.setPrice(p.getPrice());
        vo.setOriginalPrice(p.getOriginalPrice());
        vo.setImages(p.getImages() == null || p.getImages().isBlank()
                ? List.of() : Arrays.asList(p.getImages().split(",")));
        vo.setStatus(p.getStatus());
        vo.setStatusText(com.campus.market.common.ProductStatus.text(p.getStatus()));
        vo.setCreateTime(p.getCreateTime());
        return vo;
    }
}
