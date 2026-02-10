package com.sisferrete.platform.config;

import com.sisferrete.platform.audit.AuditService;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class TenantConfigService {
  private final TenantConfigRepository repository;
  private final AuditService auditService;

  public TenantConfigService(TenantConfigRepository repository, AuditService auditService) {
    this.repository = repository;
    this.auditService = auditService;
  }

  public TenantConfig getConfig(UUID tenantId) {
    return repository.findByTenantId(tenantId).orElseGet(() -> repository.insertDefault(tenantId));
  }

  public TenantConfig updateVatRate(UUID tenantId, int vatRateBps) {
    TenantConfig before = getConfig(tenantId);
    TenantConfig after = repository.updateVatRate(tenantId, vatRateBps);
    auditService.recordWithContext(
        "CONFIG_VAT_CHANGED",
        "IVA actualizado",
        "TenantConfig",
        tenantId,
        Map.of("vatRateBps", before.vatRateBps()),
        Map.of("vatRateBps", after.vatRateBps()),
        null
    );
    return after;
  }
}
