package com.example.todo;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class PerformanceAspect {

  private static final Logger log = LoggerFactory.getLogger(PerformanceAspect.class);

  @Pointcut("execution(* com.example.todo..*Service.*(..))")
  public void serviceMethods() {
  }

  @Around("serviceMethods()")
  public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.nanoTime();
    try {
      return joinPoint.proceed();
    } finally {
      long elapsedMs = (System.nanoTime() - start) / 1_000_000;
      log.info("PERF {}.{} {}ms",
          joinPoint.getSignature().getDeclaringTypeName(),
          joinPoint.getSignature().getName(),
          elapsedMs);
    }
  }
}
