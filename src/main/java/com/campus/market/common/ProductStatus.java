package com.campus.market.common;

/**
 * 商品状态机：0-待审核 1-在售 2-已下架 3-已售出
 * 只允许按讲义（第 6.1 节）里定义的合法路径流转。
 */
public final class ProductStatus {

    public static final int PENDING = 0;
    public static final int ON_SALE = 1;
    public static final int OFF_SHELF = 2;
    public static final int SOLD = 3;

    private ProductStatus() {
    }

    public static String text(Integer s) {
        if (s == null) return "未知";
        return switch (s) {
            case PENDING -> "待审核";
            case ON_SALE -> "在售";
            case OFF_SHELF -> "已下架";
            case SOLD -> "已售出";
            default -> "未知";
        };
    }
}
