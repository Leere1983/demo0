package com.example.demo0.aspect;
import com.example.demo0.entity.Log;
import com.example.demo0.repository.LogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import java.util.Arrays;
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class LogAspect {
    private final LogRepository logRepository;
    @Pointcut("execution(* com.example.demo0.controller.*.*(..))")
    public void controllerPointcut() {}
    @Pointcut("execution(* com.example.demo0.service.*.*(..))")
    public void servicePointcut() {}
    @Before("controllerPointcut()")
    public void logBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            log.info("Request URL: {}, Method: {}, Class: {}, Method: {}, Args: {}",
                    request.getRequestURL().toString(),
                    request.getMethod(),
                    joinPoint.getSignature().getDeclaringTypeName(),
                    joinPoint.getSignature().getName(),
                    Arrays.toString(joinPoint.getArgs()));
        }
    }
    @AfterReturning(pointcut = "controllerPointcut()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        log.info("Method: {} returned with value: {}",
                joinPoint.getSignature().getName(),
                result != null ? result.toString() : "null");
    }
    @AfterThrowing(pointcut = "controllerPointcut()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.error("Method: {} threw exception: {}",
                joinPoint.getSignature().getName(),
                ex.getMessage(),
                ex);
    }
    @Around("servicePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        log.debug("Entering {}.{}()", className, methodName);
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Exiting {}.{}() - Execution time: {}ms", 
                    className, methodName, duration);
            if (duration > 500) {
                log.warn("Slow method detected: {}.{}() - Execution time: {}ms",
                        className, methodName, duration);
            }
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Exception in {}.{}() - Execution time: {}ms, Error: {}",
                    className, methodName, duration, ex.getMessage());
            throw ex;
        }
    }
    @AfterReturning(pointcut = "execution(* com.example.demo0.controller.UserController.login(..))", returning = "result")
    public void logLogin(JoinPoint joinPoint, Object result) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            Log logEntry = new Log();
            logEntry.setAction("LOGIN");
            logEntry.setDescription("用户登录");
            logEntry.setIpAddress(request.getRemoteAddr());
            logRepository.save(logEntry);
        }
    }
}