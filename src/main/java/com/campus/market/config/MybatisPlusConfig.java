package com.campus.market.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 分页插件：注意 3.5.9 起 jsqlparser 被拆成独立依赖（pom 里已引入 mybatis-plus-jsqlparser），
 * 少引会在第一次分页时抛 NoClassDefFoundError。
 */
@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor page = new PaginationInnerInterceptor(DbType.MYSQL);
        page.setMaxLimit(100L);   // 单页最多 100 条，防恶意大分页
        page.setOverflow(false);
        interceptor.addInnerInterceptor(page);
        return interceptor;
    }
}
