package com.campus.market.interceptor;

import com.campus.market.annotation.RequireAdmin;
import com.campus.market.annotation.RequireLogin;
import com.campus.market.common.BusinessException;
import com.campus.market.common.UserContext;
import com.campus.market.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 鉴权拦截器（核心）：只有标了 @RequireLogin / @RequireAdmin 的接口才校验 Token，
 * 公开接口直接放行，避免把首页列表也拦成 401。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod hm)) return true;

        RequireLogin login = hm.getMethodAnnotation(RequireLogin.class);
        if (login == null) login = hm.getBeanType().getAnnotation(RequireLogin.class);

        RequireAdmin admin = hm.getMethodAnnotation(RequireAdmin.class);
        if (login == null && admin == null) return true;   // 公开接口直接放行

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }

        Claims claims;
        try {
            claims = jwtUtil.parse(header.substring(7));   // 去掉 "Bearer "
        } catch (Exception e) {
            throw new BusinessException(401, "登录已过期，请重新登录");
        }

        UserContext.LoginUser user = new UserContext.LoginUser();
        user.setId(Long.valueOf(claims.getSubject()));
        user.setRole(claims.get("role", Integer.class));
        UserContext.set(user);

        if (admin != null && !UserContext.isAdmin()) {
            throw new BusinessException(403, "无权限操作");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
