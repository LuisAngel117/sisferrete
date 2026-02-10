package com.sisferrete.auth;

import java.time.Instant;
import java.util.UUID;

public record RefreshTokenRecord(
    UUID id,
    UUID tenantId,
    UUID userId,
    String token,
    Instant expiresAt,
    Instant revokedAt,
    Instant createdAt
) {}