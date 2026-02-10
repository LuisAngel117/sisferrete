package com.sisferrete.platform.iam;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasAuthority('PERM_IAM_MANAGE')")
public class AdminRolesController {
  private final IamService iamService;

  public AdminRolesController(IamService iamService) {
    this.iamService = iamService;
  }

  @GetMapping
  public List<IamService.IamRoleResponse> listRoles(JwtAuthenticationToken authentication) {
    return iamService.listRoles(tenantId(authentication));
  }

  @PostMapping
  public IamService.IamRoleResponse createRole(
      @Valid @RequestBody IamService.CreateRoleRequest request,
      JwtAuthenticationToken authentication
  ) {
    return iamService.createRole(tenantId(authentication), request);
  }

  @PutMapping("/{roleId}")
  public IamService.IamRoleResponse updateRole(
      @PathVariable UUID roleId,
      @Valid @RequestBody IamService.UpdateRoleRequest request,
      JwtAuthenticationToken authentication
  ) {
    return iamService.updateRole(tenantId(authentication), roleId, request);
  }

  @PostMapping("/{roleId}/permissions")
  public IamService.IamRoleResponse replacePermissions(
      @PathVariable UUID roleId,
      @Valid @RequestBody IamService.ReplaceRolePermissionsRequest request,
      JwtAuthenticationToken authentication
  ) {
    return iamService.replaceRolePermissions(tenantId(authentication), roleId, request);
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
