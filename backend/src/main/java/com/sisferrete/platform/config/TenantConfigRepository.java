package com.sisferrete.platform.config;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TenantConfigRepository {
  private static final int DEFAULT_VAT_BPS = 1500;
  private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

  private final JdbcTemplate jdbc;

  public TenantConfigRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<TenantConfig> findByTenantId(UUID tenantId) {
    String sql = "select tenant_id, vat_rate_bps from tenant_config where tenant_id = ?";
    List<TenantConfig> configs = jdbc.query(sql, (rs, rowNum) ->
        new TenantConfig(
            rs.getObject("tenant_id", UUID.class),
            rs.getInt("vat_rate_bps")
        ), tenantId);
    if (configs.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(configs.get(0));
  }

  public TenantConfig insertDefault(UUID tenantId) {
    insertOrUpdate(tenantId, DEFAULT_VAT_BPS);
    return new TenantConfig(tenantId, DEFAULT_VAT_BPS);
  }

  public TenantConfig updateVatRate(UUID tenantId, int vatRateBps) {
    insertOrUpdate(tenantId, vatRateBps);
    return new TenantConfig(tenantId, vatRateBps);
  }

  private void insertOrUpdate(UUID tenantId, int vatRateBps) {
    BigDecimal vatRate = BigDecimal.valueOf(vatRateBps).divide(ONE_HUNDRED);
    String updateSql = """
        update tenant_config
        set vat_rate_bps = ?, vat_rate = ?, updated_at = now()
        where tenant_id = ?
        """;
    int updated = jdbc.update(updateSql, vatRateBps, vatRate, tenantId);
    if (updated == 0) {
      String insertSql = """
          insert into tenant_config (id, tenant_id, vat_rate, vat_rate_bps, created_at, updated_at)
          values (gen_random_uuid(), ?, ?, ?, now(), now())
          """;
      jdbc.update(insertSql, tenantId, vatRate, vatRateBps);
    }
  }
}
