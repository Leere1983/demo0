package com.example.demo0.aspect;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
@Aspect
@Component
@Slf4j
public class PerformanceAspect {
    private final ConcurrentHashMap<String, Long> methodCallCount = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> methodTotalTime = new ConcurrentHashMap<>();
    @Pointcut("execution(* com.example.demo0.service.*.*(..))")
    public void serviceMethods() {}
    @Pointcut("execution(* com.example.demo0.repository.*.*(..))")
    public void repositoryMethods() {}
    @Around("serviceMethods() || repositoryMethods()")
    public Object measurePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        String methodKey = joinPoint.getSignature().toLongString();
        try {
            return joinPoint.proceed();
        } finally {
            long durationNanos = System.nanoTime() - startTime;
            long durationMs = durationNanos / 1_000_000;
            methodCallCount.merge(methodKey, 1L, Long::sum);
            methodTotalTime.merge(methodKey, durationMs, Long::sum);
            if (durationMs > 100) {
                log.warn("Slow execution detected - Method: {}, Duration: {}ms",
                        methodKey, durationMs);
            }
            if (methodCallCount.get(methodKey) % 100 == 0) {
                long avgTime = methodTotalTime.get(methodKey) / methodCallCount.get(methodKey);
                log.info("Performance statistics for {} - Calls: {}, Avg time: {}ms",
                        methodKey, methodCallCount.get(methodKey), avgTime);
            }
        }
    }
}