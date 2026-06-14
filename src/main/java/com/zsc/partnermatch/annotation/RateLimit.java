package com.zsc.partnermatch.annotation;

import java.lang.annotation.*;

/**
 * 接口限流注解
 * 基于 Guava RateLimiter 实现令牌桶限流
 * 
 * 使用示例：
 * @RateLimit(qps = 5, fallbackMsg = "系统繁忙，请稍后再试")
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {

    /**
     * 每秒允许的请求数（QPS）
     */
    double qps() default 5.0;

    /**
     * 限流触发时的提示消息
     */
    String fallbackMsg() default "系统繁忙，请稍后再试";

    /**
     * 获取令牌的最大等待时间（毫秒），0 表示不等待
     */
    long timeoutMs() default 0;
}
