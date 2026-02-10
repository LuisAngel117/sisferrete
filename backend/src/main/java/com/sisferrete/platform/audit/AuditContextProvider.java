package com.sisferrete.platform.audit;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuditContextProvider {

  public AuditContext current() {
    RequestMetadata metadata = RequestMetadataHolder.get();
    UUID branchId = parseUuid(metadata != null ? metadata.branchId() : null);
    String ip = metadata != null ? metadata.ipAddress() : null;
    String userAgent = metadata != null ? metadata.userAgent() : null;

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof JwtAuthenticationToken token) {
      Jwt jwt = token.getToken();
      UUID tenantId = parseUuid(jwt.getClaimAsString("tenant_id"));
      UUID userId = parseUuid(jwt.getClaimAsString("user_id"));
      String email = jwt.getClaimAsString("email");
      return new AuditContext(tenantId, branchId, userId, email, ip, userAgent);
    }

    return new AuditContext(null, branchId, null, null, ip, userAgent);
  }

  private UUID parseUuid(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      return null;
    }
  }
}
