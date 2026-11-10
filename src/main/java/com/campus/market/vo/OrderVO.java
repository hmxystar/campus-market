package com.campus.market.vo;

import com.campus.market.common.OrderStatus;
import com.campus.market.entity.Order;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {

    private Long id;
    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    private String productTitle;
    private String productCover;
    private BigDecimal amount;
    private Integer status;
    private String statusText;
    private LocalDateTime expireTime;
    private LocalDateTime createTime;

    public static OrderVO of(Order o) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId());
        vo.setOrderNo(o.getOrderNo());
        vo.setBuyerId(o.getBuyerId());
        vo.setSellerId(o.getSellerId());
        vo.setProductId(o.getProductId());
        vo.setProductTitle(o.getProductTitle());
        vo.setProductCover(o.getProductCover());
        vo.setAmount(o.getAmount());
        vo.setStatus(o.getStatus());
        vo.setStatusText(OrderStatus.text(o.getStatus()));
        vo.setExpireTime(o.getExpireTime());
        vo.setCreateTime(o.getCreateTime());
        return vo;
    }
}
