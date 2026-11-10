package com.campus.market;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 骨架自检：能加载 Spring 容器即说明配置、Mapper、Bean 依赖没断。
 * 跑之前确保 MySQL / Redis 已启动且已执行 sql/init.sql。
 */
@SpringBootTest
class CampusMarketApplicationTests {

    @Test
    void contextLoads() {
    }
}
