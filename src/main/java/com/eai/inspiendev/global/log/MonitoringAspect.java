package com.eai.inspiendev.global.log;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class MonitoringAspect {

    @Around("@annotation(monitoringLog)")
    public Object logExecution(ProceedingJoinPoint joinPoint, MonitoringLog monitoringLog) throws Throwable {

        String taskName = monitoringLog.value();
        long start = System.currentTimeMillis();

        log.info("[START] 작업명: {} | 메서드: {}", taskName, joinPoint.getSignature().toShortString());

        try {
            Object result = joinPoint.proceed();
            long elapsed = System.currentTimeMillis() - start;

            log.info("[SUCCESS] 작업명: {} | 소요시간: {}ms", taskName, elapsed);
            return result;

        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - start;

            log.error("[FAIL] 작업명: {} | 소요시간: {}ms | 에러: {}", taskName, elapsed, e.getMessage(), e);
            throw e;
        }
    }
}