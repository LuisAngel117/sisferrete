package com.sisferrete.catalog;

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
@RequestMapping("/api/admin")
@PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
public class AdminPackagingController {
  private final PackagingService service;

  public AdminPackagingController(PackagingService service) {
    this.service = service;
  }

  @GetMapping("/products/{productId}/packagings")
  public List<PackagingService.PackagingResponse> listPackagings(
      @PathVariable UUID productId,
      JwtAuthenticationToken authentication
  ) {
    return service.listPackagings(tenantId(authentication), productId);
  }

  @PostMapping("/products/{productId}/packagings")
  public PackagingService.PackagingResponse createPackaging(
      @PathVariable UUID productId,
      @Valid @RequestBody PackagingService.PackagingRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.createPackaging(tenantId(authentication), productId, request);
  }

  @PutMapping("/packagings/{packagingId}")
  public PackagingService.PackagingResponse updatePackaging(
      @PathVariable UUID packagingId,
      @Valid @RequestBody PackagingService.PackagingRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.updatePackaging(tenantId(authentication), packagingId, request);
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
