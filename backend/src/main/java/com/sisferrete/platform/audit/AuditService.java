package com.sisferrete.platform.audit;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
  private static final Logger log = LoggerFactory.getLogger(AuditService.class);

  private final AuditRepository auditRepository;
  private final AuditContextProvider contextProvider;

  public AuditService(AuditRepository auditRepository, AuditContextProvider contextProvider) {
    this.auditRepository = auditRepository;
    this.contextProvider = contextProvider;
  }

  public void recordWithContext(String actionCode, String summary, String entityType, UUID entityId,
                                Object beforeState, Object afterState, Object metadata) {
    AuditContext context = contextProvider.current();
    if (context.tenantId() == null) {
      log.warn("Audit omitido: tenantId ausente para actionCode={}", actionCode);
      return;
    }
    AuditEvent event = new AuditEvent(
        UUID.randomUUID(),
        context.tenantId(),
        context.branchId(),
        context.actorUserId(),
        context.actorEmail(),
        actionCode,
        entityType,
        entityId,
        summary,
        beforeState,
        afterState,
        metadata,
        context.ipAddress(),
        context.userAgent(),
        Instant.now()
    );
    auditRepository.insert(event);
  }

  public void recordAuthSuccess(UUID tenantId, UUID userId, String email, String actionCode) {
    recordForUser(tenantId, userId, email, actionCode, null, null);
  }

  public void recordForUser(UUID tenantId, UUID userId, String email, String actionCode,
                            String summary, Map<String, Object> metadata) {
    if (tenantId == null) {
      log.warn("Audit omitido: tenantId ausente para actionCode={}", actionCode);
      return;
    }
    AuditContext context = contextProvider.current();
    AuditEvent event = new AuditEvent(
        UUID.randomUUID(),
        tenantId,
        context.branchId(),
        userId,
        email,
        actionCode,
        null,
        null,
        summary,
        null,
        null,
        metadata,
        context.ipAddress(),
        context.userAgent(),
        Instant.now()
    );
    auditRepository.insert(event);
  }
}
