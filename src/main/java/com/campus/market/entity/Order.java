package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 表名是 orders（order 是 SQL 关键字，避开） */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("orders")
public class Order extends BaseEntity {

    private String orderNo;
    private Long buyerId;
    private Long sellerId;
    private Long productId;
    /** 冗余快照，商品改价/改名后订单仍显示下单时的信息 */
    private String productTitle;
    private String productCover;
    private BigDecimal amount;
    /** 见 OrderStatus */
    private Integer status;
    /** 超时取消时间点 */
    private LocalDateTime expireTime;
    private LocalDateTime payTime;
    private LocalDateTime finishTime;
    private LocalDateTime cancelTime;
}
