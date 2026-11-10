package com.campus.market.common;

/**
 * 订单状态机：0-待付款 1-待发货 2-已完成 3-已取消
 */
public final class OrderStatus {

    public static final int WAIT_PAY = 0;
    public static final int WAIT_SEND = 1;
    public static final int FINISHED = 2;
    public static final int CANCELED = 3;

    private OrderStatus() {
    }

    public static String text(Integer s) {
        if (s == null) return "未知";
        return switch (s) {
            case WAIT_PAY -> "待付款";
            case WAIT_SEND -> "待发货";
            case FINISHED -> "已完成";
            case CANCELED -> "已取消";
            default -> "未知";
        };
    }
}
