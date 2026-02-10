package com.sisferrete.platform.audit;

import java.util.UUID;

public record AuditContext(
    UUID tenantId,
    UUID branchId,
    UUID actorUserId,
    String actorEmail,
    String ipAddress,
    String userAgent
) {}
