package com.campus.market.aspect;

import com.campus.market.annotation.RateLimit;
import com.campus.market.common.BusinessException;
import com.campus.market.common.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * 限流切面：Redis + Lua 固定窗口。
 * INCR 与 EXPIRE 放在同一段 Lua 里，保证原子性，避免「刚 INCR 还没 EXPIRE」的窗口漏洞。
 */
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    /** KEYS[1]=key ARGV[1]=limit ARGV[2]=seconds */
    private static final DefaultRedisScript<Long> SCRIPT = new DefaultRedisScript<>(
            "local c = redis.call('incr', KEYS[1]) " +
            "if c == 1 then redis.call('expire', KEYS[1], ARGV[2]) end " +
            "if c > tonumber(ARGV[1]) then return 0 else return 1 end", Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    @Around("@annotation(rateLimit)")
    public Object around(ProceedingJoinPoint pjp, RateLimit rateLimit) throws Throwable {
        String key = buildKey(pjp, rateLimit);
        Long ok = stringRedisTemplate.execute(SCRIPT, List.of(key),
                String.valueOf(rateLimit.limit()), String.valueOf(rateLimit.seconds()));
        if (ok == null || ok == 0L) {
            throw new BusinessException(429, "操作太频繁，请稍后再试");
        }
        return pjp.proceed();
    }

    /** 已登录按 userId 限流，未登录按 IP 限流 */
    private String buildKey(ProceedingJoinPoint pjp, RateLimit rateLimit) {
        MethodSignature sig = (MethodSignature) pjp.getSignature();
        String biz = sig.getDeclaringType().getSimpleName() + ":" + sig.getName();
        Long userId = UserContext.getUserId();
        String who = userId != null ? "u" + userId : "ip" + clientIp();
        return "rate:" + biz + ":" + who;
    }

    private String clientIp() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return "unknown";
        HttpServletRequest req = attrs.getRequest();
        String xff = req.getHeader("X-Forwarded-For");
        return (xff == null || xff.isBlank()) ? req.getRemoteAddr() : xff.split(",")[0].trim();
    }
}
