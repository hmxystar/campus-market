package com.campus.market.task;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.campus.market.entity.Product;
import com.campus.market.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 浏览数批量落库：详情页只 INCR Redis，每 5 分钟把增量写回 MySQL，
 * 把「每次访问一条 UPDATE」变成「5 分钟一批 UPDATE」。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ViewCountFlushTask {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMapper productMapper;

    @Scheduled(fixedDelay = 300000)      // 每 5 分钟
    public void flushViewCount() {
        Set<String> keys = redisTemplate.keys("product:view:*");
        if (keys == null || keys.isEmpty()) return;

        for (String key : keys) {
            Object delta = redisTemplate.opsForValue().getAndDelete(key);   // 取走并删除，原子
            if (delta == null) continue;

            long inc = Long.parseLong(delta.toString());
            if (inc <= 0) continue;

            Long id = Long.valueOf(key.substring("product:view:".length()));
            productMapper.update(null,
                    Wrappers.<Product>lambdaUpdate()
                            .setSql("view_count = view_count + " + inc)
                            .eq(Product::getId, id));
        }
        log.debug("浏览数落库完成，共处理 {} 个 key", keys.size());
    }
}
