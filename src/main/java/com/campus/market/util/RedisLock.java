package com.campus.market.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Redis 分布式锁：SETNX + UUID 标识 + Lua 脚本释放（只删自己加的锁）。
 */
@Component
@RequiredArgsConstructor
public class RedisLock {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "  return redis.call('del', KEYS[1]) " +
            "else return 0 end", Long.class);

    private final StringRedisTemplate stringRedisTemplate;

    /** 返回锁标识；获取失败返回 null */
    public String tryLock(String key, Duration timeout) {
        String value = UUID.randomUUID().toString();
        Boolean ok = stringRedisTemplate.opsForValue().setIfAbsent(key, value, timeout);
        return Boolean.TRUE.equals(ok) ? value : null;
    }

    /** 只删自己加的锁 */
    public void unlock(String key, String value) {
        if (value == null) return;
        stringRedisTemplate.execute(UNLOCK_SCRIPT, List.of(key), value);
    }
}
