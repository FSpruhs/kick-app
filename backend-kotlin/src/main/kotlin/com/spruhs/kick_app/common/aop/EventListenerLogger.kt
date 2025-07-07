package com.spruhs.kick_app.common.aop

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Aspect
@Component
class EventListenerLoggingAspect {

    private val log = LoggerFactory.getLogger(this::class.java)

    @Pointcut("@annotation(org.springframework.context.event.EventListener)")
    fun eventListenerMethod() {}

    @Around("eventListenerMethod()")
    fun logEventListener(joinPoint: ProceedingJoinPoint): Any? {
        val methodName = joinPoint.signature.name
        val targetClass = joinPoint.target::class.simpleName
        val args = joinPoint.args

        if (args.isNotEmpty()) {
            val event = args[0]
            log.info("EventListener [$targetClass#$methodName] received event: $event")
        } else {
            log.info("EventListener [$targetClass#$methodName] invoked with no arguments.")
        }

        return joinPoint.proceed()
    }
}
