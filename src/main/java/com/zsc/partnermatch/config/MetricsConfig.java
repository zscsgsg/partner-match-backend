package com.zsc.partnermatch.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 监控指标配置
 * 注册自定义业务指标，供 Prometheus 采集
 */
@Configuration
public class MetricsConfig {

    /**
     * 匹配请求计数器
     */
    @Bean
    public Counter matchRequestCounter(MeterRegistry registry) {
        return Counter.builder("match.request.count")
                .description("智能匹配请求总次数")
                .tag("type", "smart")
                .register(registry);
    }

    /**
     * 匹配接口耗时计时器
     */
    @Bean
    public Timer matchRequestTimer(MeterRegistry registry) {
        return Timer.builder("match.request.duration")
                .description("智能匹配接口耗时")
                .tag("type", "smart")
                .register(registry);
    }

    /**
     * AI 调用成功计数器
     */
    @Bean
    public Counter aiCallSuccessCounter(MeterRegistry registry) {
        return Counter.builder("ai.call.success")
                .description("AI 调用成功次数")
                .register(registry);
    }

    /**
     * AI 调用失败计数器
     */
    @Bean
    public Counter aiCallFailCounter(MeterRegistry registry) {
        return Counter.builder("ai.call.fail")
                .description("AI 调用失败次数")
                .register(registry);
    }

    /**
     * 缓存命中计数器
     */
    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("cache.hit")
                .description("缓存命中次数")
                .register(registry);
    }

    /**
     * 缓存未命中计数器
     */
    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("cache.miss")
                .description("缓存未命中次数")
                .register(registry);
    }
}
