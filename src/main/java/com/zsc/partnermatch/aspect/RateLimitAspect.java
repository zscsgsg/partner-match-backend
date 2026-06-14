package com.zsc.partnermatch.aspect;

import com.google.common.util.concurrent.RateLimiter;
import com.zsc.partnermatch.annotation.RateLimit;
import com.zsc.partnermatch.commont.ErrorCode;
import com.zsc.partnermatch.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 接口限流 AOP 切面
 * 基于 Guava RateLimiter 的令牌桶算法，对标注 @RateLimit 的方法进行 QPS 限流
 */
@Aspect
@Component
@Slf4j
public class RateLimitAspect {

    /**
     * 方法级别的 RateLimiter 缓存（key = 方法全限定名）
     */
    private final Map<String, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    @Around("@annotation(com.zsc.partnermatch.annotation.RateLimit)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getDeclaringTypeName() + "." + signature.getName();

        RateLimit rateLimit = signature.getMethod().getAnnotation(RateLimit.class);
        double qps = rateLimit.qps();
        long timeoutMs = rateLimit.timeoutMs();

        // 获取或创建 RateLimiter（懒加载，线程安全）
        RateLimiter rateLimiter = rateLimiterMap.computeIfAbsent(methodName,
                k -> RateLimiter.create(qps));

        // 尝试获取令牌
        boolean acquired;
        if (timeoutMs > 0) {
            acquired = rateLimiter.tryAcquire(timeoutMs, TimeUnit.MILLISECONDS);
        } else {
            acquired = rateLimiter.tryAcquire();
        }

        if (!acquired) {
            log.warn("[限流触发] method={} qps={}", methodName, qps);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, rateLimit.fallbackMsg());
        }

        return joinPoint.proceed();
    }
}
