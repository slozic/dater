package com.slozic.dater.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
@Slf4j
public class ServiceLoggingAspect {

    @Pointcut("execution(public * com.slozic.dater.services.dateevent.DateEventService.deleteDateEvent(..))")
    public void deleteDateEventMethod() {
    }

    @Pointcut("execution(public * com.slozic.dater.services.dateevent.DateEventService.createDateEventWithDefaultAttendee(..))")
    public void createDateEventWithDefaultAttendee() {
    }

    @Pointcut("execution(public * com.slozic.dater.services.images.DateEventImageService.deleteAllImages(..))")
    public void deleteAllImages() {
    }

    @AfterReturning(pointcut = "deleteDateEventMethod()", returning = "result")
    public void log_deleteDateEvent_MethodReturning(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        log.info("Deleting date event with ID {} ", Arrays.toString(joinPoint.getArgs()));
    }

    @AfterReturning(pointcut = "createDateEventWithDefaultAttendee()", returning = "result")
    public void log_createDateEvent_MethodReturning(JoinPoint joinPoint, Object result) {
        log.info("New date event created {}", result);
    }

    @AfterReturning(pointcut = "deleteAllImages()", returning = "result")
    public void log_deleteAllImages_MethodReturning(JoinPoint joinPoint, Object result) {
        log.info("Deleted all images for date event with id {} ", Arrays.toString(joinPoint.getArgs()));
    }

}

