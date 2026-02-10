package com.sisferrete.auth;

import java.util.UUID;

public record UserAccount(
    UUID id,
    UUID tenantId,
    String email,
    String fullName,
    String passwordHash,
    boolean twoFactorEnabled,
    String twoFactorSecret,
    boolean active
) {}