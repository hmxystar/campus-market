package com.campus.market.common;

import lombok.Data;

/**
 * 当前登录用户上下文：拦截器解析完 JWT 后写入 ThreadLocal，
 * Service / Controller 直接取，不用层层传参。
 * 注意：请求结束（afterCompletion）必须 clear，否则线程池复用会串用户。
 */
public class UserContext {

    @Data
    public static class LoginUser {
        private Long id;
        /** 0-用户 1-管理员 */
        private Integer role;
    }

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        LoginUser u = HOLDER.get();
        return u == null ? null : u.getId();
    }

    public static boolean isAdmin() {
        LoginUser u = HOLDER.get();
        return u != null && Integer.valueOf(1).equals(u.getRole());
    }

    public static void clear() {
        HOLDER.remove();
    }
}
