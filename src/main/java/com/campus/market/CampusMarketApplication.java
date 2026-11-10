package com.campus.market;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 启动类：三个注解分别启用了什么
 *  - @EnableAsync      允许 @Async 异步方法（操作日志落库用）
 *  - @EnableScheduling 允许 @Scheduled 定时任务（超时订单 / 浏览数落库用）
 *  - @MapperScan       扫描 Mapper 接口，省去每个接口写 @Mapper
 */
@EnableAsync
@EnableScheduling
@MapperScan("com.campus.market.mapper")
@SpringBootApplication
public class CampusMarketApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusMarketApplication.class, args);
    }
}
