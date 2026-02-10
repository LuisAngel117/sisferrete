package com.sisferrete.platform.iam;

import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/branches")
@PreAuthorize("hasAuthority('PERM_IAM_MANAGE')")
public class AdminBranchesController {
  private final IamService iamService;

  public AdminBranchesController(IamService iamService) {
    this.iamService = iamService;
  }

  @GetMapping
  public List<IamService.IamBranchResponse> listBranches(JwtAuthenticationToken authentication) {
    return iamService.listBranches(tenantId(authentication));
  }

  private UUID tenantId(JwtAuthenticationToken authentication) {
    String value = authentication.getToken().getClaimAsString("tenant_id");
    if (value == null || value.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no encontrado");
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant inválido");
    }
  }
}
