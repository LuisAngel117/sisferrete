package com.sisferrete.platform.config;

import java.util.UUID;

public record TenantConfig(
    UUID tenantId,
    int vatRateBps
) {}
