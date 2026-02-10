package com.sisferrete.platform.audit;

import java.time.Instant;
import java.util.UUID;

public record AuditEvent(
    UUID id,
    UUID tenantId,
    UUID branchId,
    UUID actorUserId,
    String actorEmail,
    String actionCode,
    String entityType,
    UUID entityId,
    String summary,
    Object beforeState,
    Object afterState,
    Object metadata,
    String ipAddress,
    String userAgent,
    Instant createdAt
) {}
