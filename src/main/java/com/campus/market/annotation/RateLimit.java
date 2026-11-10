package com.campus.market.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 接口限流：固定窗口，默认 60 秒内最多 10 次 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /** 窗口内最大次数 */
    int limit() default 10;

    /** 窗口长度（秒） */
    int seconds() default 60;
}
