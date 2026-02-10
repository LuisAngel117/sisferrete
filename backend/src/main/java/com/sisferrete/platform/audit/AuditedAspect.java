package com.sisferrete.platform.audit;

import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditedAspect {
  private final AuditService auditService;

  public AuditedAspect(AuditService auditService) {
    this.auditService = auditService;
  }

  @AfterReturning("@annotation(audited)")
  public void afterSuccess(JoinPoint joinPoint, Audited audited) {
    String entityType = audited.entityType().isBlank() ? null : audited.entityType();
    String summary = audited.summary().isBlank() ? null : audited.summary();
    auditService.recordWithContext(
        audited.actionCode(),
        summary,
        entityType,
        null,
        null,
        null,
        Map.of("source", "aop")
    );
  }
}
