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
@RequestMapping("/api/admin/users")
@PreAuthorize("hasAuthority('PERM_IAM_MANAGE')")
public class AdminUsersController {
  private final IamService iamService;

  public AdminUsersController(IamService iamService) {
    this.iamService = iamService;
  }

  @GetMapping
  public List<IamService.IamUserResponse> listUsers(JwtAuthenticationToken authentication) {
    return iamService.listUsers(tenantId(authentication));
  }

  @PostMapping
  public IamService.IamUserResponse createUser(
      @Valid @RequestBody IamService.CreateUserRequest request,
      JwtAuthenticationToken authentication
  ) {
    return iamService.createUser(tenantId(authentication), request);
  }

  @GetMapping("/{userId}")
  public IamService.IamUserResponse getUser(
      @PathVariable UUID userId,
      JwtAuthenticationToken authentication
  ) {
    return iamService.getUser(tenantId(authentication), userId);
  }

  @PutMapping("/{userId}")
  public IamService.IamUserResponse updateUser(
      @PathVariable UUID userId,
      @Valid @RequestBody IamService.UpdateUserRequest request,
      JwtAuthenticationToken authentication
  ) {
    return iamService.updateUser(tenantId(authentication), userId, request);
  }

  @PostMapping("/{userId}/roles")
  public IamService.IamUserResponse replaceRoles(
      @PathVariable UUID userId,
      @Valid @RequestBody IamService.ReplaceUserRolesRequest request,
      JwtAuthenticationToken authentication
  ) {
    return iamService.replaceUserRoles(tenantId(authentication), userId, request);
  }

  @PostMapping("/{userId}/branches")
  public IamService.IamUserResponse replaceBranches(
      @PathVariable UUID userId,
      @RequestBody IamService.ReplaceUserBranchesRequest request,
      JwtAuthenticationToken authentication
  ) {
    return iamService.replaceUserBranches(tenantId(authentication), userId, request);
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
