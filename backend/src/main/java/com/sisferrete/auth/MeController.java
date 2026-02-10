package com.sisferrete.auth;

import java.util.List;
import java.util.UUID;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MeController {

  @GetMapping("/me")
  public MeResponse me(JwtAuthenticationToken authentication) {
    Jwt jwt = authentication.getToken();
    return new MeResponse(
        jwt.getClaimAsString("user_id"),
        jwt.getClaimAsString("email"),
        jwt.getClaimAsString("full_name"),
        jwt.getClaimAsString("tenant_id"),
        getStringList(jwt, "roles"),
        getStringList(jwt, "permissions"),
        getStringList(jwt, "branch_access")
    );
  }

  @GetMapping("/secure/ping")
  public SecurePingResponse securePing(
      JwtAuthenticationToken authentication,
      @RequestHeader(name = "X-Branch-Id", required = false) String branchId
  ) {
    Jwt jwt = authentication.getToken();
    if (branchId != null && !branchId.isBlank()) {
      boolean allowed = getStringList(jwt, "branch_access").stream().anyMatch(id -> id.equals(branchId));
      if (!allowed) {
        throw new AccessDeniedException("Sin acceso a sucursal");
      }
    }
    return new SecurePingResponse(true, jwt.getClaimAsString("tenant_id"), jwt.getClaimAsString("user_id"));
  }

  private List<String> getStringList(Jwt jwt, String claim) {
    List<String> list = jwt.getClaim(claim);
    return list == null ? List.of() : list;
  }

  public record MeResponse(
      String userId,
      String email,
      String fullName,
      String tenantId,
      List<String> roles,
      List<String> permissions,
      List<String> branchAccess
  ) {}

  public record SecurePingResponse(boolean ok, String tenantId, String userId) {}
}