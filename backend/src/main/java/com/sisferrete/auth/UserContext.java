package com.sisferrete.auth;

import java.util.List;
import java.util.UUID;

public record UserContext(
    UUID userId,
    UUID tenantId,
    String email,
    String fullName,
    List<String> roles,
    List<String> permissions,
    List<UUID> branchAccess
) {}