package com.example.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.example.demo.controller.*.*(..))")
    public void controllerMethods() {}

    @Pointcut("execution(* com.example.demo.service.*.*(..))")
    public void serviceMethods() {}

    @Before("controllerMethods()")
    public void logControllerEntry(JoinPoint joinPoint) {
        log.info("Entering controller method: {} with arguments: {}", 
                joinPoint.getSignature().getName(), 
                joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "controllerMethods()", returning = "result")
    public void logControllerExit(JoinPoint joinPoint, Object result) {
        log.info("Exiting controller method: {} with result: {}", 
                joinPoint.getSignature().getName(), 
                result);
    }

    @AfterThrowing(pointcut = "controllerMethods()", throwing = "ex")
    public void logControllerException(JoinPoint joinPoint, Throwable ex) {
        log.error("Exception in controller method: {} - {}", 
                joinPoint.getSignature().getName(), 
                ex.getMessage(), 
                ex);
    }

    @Around("serviceMethods()")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        log.info("Starting service method: {}", joinPoint.getSignature().getName());
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            log.info("Completed service method: {} in {}ms", 
                    joinPoint.getSignature().getName(), 
                    duration);
            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Failed service method: {} in {}ms - {}", 
                    joinPoint.getSignature().getName(), 
                    duration, 
                    ex.getMessage());
            throw ex;
        }
    }
}