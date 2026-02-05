package com.example.todo;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

  private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

  @Pointcut("execution(* com.example.todo..*Service.*(..))")
  public void serviceMethods() {
  }

  @Before("serviceMethods()")
  public void before(JoinPoint joinPoint) {
    log.info("START {}.{} args={}",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
        joinPoint.getArgs());
  }

  @AfterReturning(pointcut = "serviceMethods()", returning = "result")
  public void afterReturning(JoinPoint joinPoint, Object result) {
    log.info("END {}.{} result={}",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
        result);
  }

  @AfterThrowing(pointcut = "serviceMethods()", throwing = "ex")
  public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
    log.warn("EXCEPTION {}.{} message={}",
        joinPoint.getSignature().getDeclaringTypeName(),
        joinPoint.getSignature().getName(),
        ex.getMessage(),
        ex);
  }
}
