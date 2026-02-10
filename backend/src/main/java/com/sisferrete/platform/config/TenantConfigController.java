package com.sisferrete.platform.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/tenant-config")
public class TenantConfigController {
  private final TenantConfigService service;

  public TenantConfigController(TenantConfigService service) {
    this.service = service;
  }

  @GetMapping
  @PreAuthorize("hasAuthority('PERM_CONFIG_VAT_EDIT') or hasAuthority('PERM_CONFIG_VIEW')")
  public TenantConfigResponse getConfig(JwtAuthenticationToken authentication) {
    UUID tenantId = extractTenantId(authentication);
    TenantConfig config = service.getConfig(tenantId);
    return toResponse(config);
  }

  @PutMapping("/vat")
  @PreAuthorize("hasAuthority('PERM_CONFIG_VAT_EDIT')")
  public TenantConfigResponse updateVat(
      @Valid @RequestBody UpdateVatRequest request,
      JwtAuthenticationToken authentication
  ) {
    UUID tenantId = extractTenantId(authentication);
    TenantConfig updated = service.updateVatRate(tenantId, request.vatRateBps());
    return toResponse(updated);
  }

  private TenantConfigResponse toResponse(TenantConfig config) {
    BigDecimal percent = BigDecimal.valueOf(config.vatRateBps())
        .movePointLeft(2)
        .setScale(2, RoundingMode.HALF_UP);
    return new TenantConfigResponse(
        config.tenantId(),
        config.vatRateBps(),
        percent.toPlainString()
    );
  }

  private UUID extractTenantId(JwtAuthenticationToken authentication) {
    String tenantId = authentication.getToken().getClaimAsString("tenant_id");
    if (tenantId == null || tenantId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no encontrado");
    }
    try {
      return UUID.fromString(tenantId);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant inválido");
    }
  }

  public record TenantConfigResponse(
      UUID tenantId,
      int vatRateBps,
      String vatRatePercent
  ) {}

  public record UpdateVatRequest(
      @NotNull @Min(0) @Max(2500) Integer vatRateBps
  ) {}
}
