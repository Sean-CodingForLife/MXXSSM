package com.mxxssm.common.audit;

import com.mxxssm.common.security.SecurityUtils;
import com.mxxssm.common.web.ClientIp;
import com.mxxssm.modules.log.LogService;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    private final LogService logService;

    public AuditLogAspect(LogService logService) {
        this.logService = logService;
    }

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        long startedAt = System.nanoTime();
        Throwable failure = null;
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            failure = throwable;
            throw throwable;
        } finally {
            writeAudit(auditLog, failure, Duration.ofNanos(System.nanoTime() - startedAt).toMillis());
        }
    }

    private void writeAudit(AuditLog auditLog, Throwable failure, long durationMs) {
        try {
            if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
                return;
            }
            HttpServletRequest request = attributes.getRequest();
            String message = failure == null ? "OK" : failure.getMessage();
            logService.recordOperation(
                    SecurityUtils.currentUsername(),
                    auditLog.action(),
                    request.getMethod(),
                    request.getRequestURI(),
                    failure == null ? "SUCCESS" : "FAIL",
                    message,
                    durationMs,
                    ClientIp.resolve(request));
        } catch (RuntimeException exception) {
            log.warn("Unable to write operation audit log", exception);
        }
    }
}
