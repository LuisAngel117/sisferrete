package com.sisferrete.platform.audit;

public record RequestMetadata(
    String ipAddress,
    String userAgent,
    String branchId,
    String path,
    String method
) {}
