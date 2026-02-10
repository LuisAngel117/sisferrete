package com.sisferrete.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PackagingRepository {
  private final JdbcTemplate jdbc;

  public PackagingRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<PackagingRecord> listPackagings(UUID tenantId, UUID productId) {
    String sql = """
        select id, product_id, variant_id, sale_uom_id, base_uom_id,
               base_units_per_sale_unit, barcode, is_default_for_sale, is_active
        from product_packagings
        where tenant_id = ? and product_id = ?
        order by sale_uom_id
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new PackagingRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getObject("variant_id", UUID.class),
            rs.getObject("sale_uom_id", UUID.class),
            rs.getObject("base_uom_id", UUID.class),
            rs.getBigDecimal("base_units_per_sale_unit"),
            rs.getString("barcode"),
            rs.getBoolean("is_default_for_sale"),
            rs.getBoolean("is_active")
        ), tenantId, productId);
  }

  public Optional<PackagingRecord> findPackaging(UUID tenantId, UUID packagingId) {
    String sql = """
        select id, product_id, variant_id, sale_uom_id, base_uom_id,
               base_units_per_sale_unit, barcode, is_default_for_sale, is_active
        from product_packagings
        where tenant_id = ? and id = ?
        """;
    List<PackagingRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new PackagingRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getObject("variant_id", UUID.class),
            rs.getObject("sale_uom_id", UUID.class),
            rs.getObject("base_uom_id", UUID.class),
            rs.getBigDecimal("base_units_per_sale_unit"),
            rs.getString("barcode"),
            rs.getBoolean("is_default_for_sale"),
            rs.getBoolean("is_active")
        ), tenantId, packagingId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<PackagingRecord> findPackagingByBarcode(UUID tenantId, String barcode) {
    String sql = """
        select id, product_id, variant_id, sale_uom_id, base_uom_id,
               base_units_per_sale_unit, barcode, is_default_for_sale, is_active
        from product_packagings
        where tenant_id = ? and barcode = ?
        """;
    List<PackagingRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new PackagingRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getObject("variant_id", UUID.class),
            rs.getObject("sale_uom_id", UUID.class),
            rs.getObject("base_uom_id", UUID.class),
            rs.getBigDecimal("base_units_per_sale_unit"),
            rs.getString("barcode"),
            rs.getBoolean("is_default_for_sale"),
            rs.getBoolean("is_active")
        ), tenantId, barcode);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<PackagingRecord> findPackagingByComposite(
      UUID tenantId,
      UUID productId,
      UUID variantId,
      UUID saleUomId
  ) {
    String sql = """
        select id, product_id, variant_id, sale_uom_id, base_uom_id,
               base_units_per_sale_unit, barcode, is_default_for_sale, is_active
        from product_packagings
        where tenant_id = ?
          and product_id = ?
          and variant_id is not distinct from ?
          and sale_uom_id = ?
        """;
    List<PackagingRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new PackagingRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getObject("variant_id", UUID.class),
            rs.getObject("sale_uom_id", UUID.class),
            rs.getObject("base_uom_id", UUID.class),
            rs.getBigDecimal("base_units_per_sale_unit"),
            rs.getString("barcode"),
            rs.getBoolean("is_default_for_sale"),
            rs.getBoolean("is_active")
        ), tenantId, productId, variantId, saleUomId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public UUID insertPackaging(
      UUID tenantId,
      UUID productId,
      UUID variantId,
      UUID saleUomId,
      UUID baseUomId,
      BigDecimal baseUnitsPerSaleUnit,
      String barcode,
      boolean isDefaultForSale,
      boolean isActive
  ) {
    UUID id = UUID.randomUUID();
    String sql = """
        insert into product_packagings (
          id, tenant_id, product_id, variant_id, sale_uom_id, base_uom_id,
          base_units_per_sale_unit, barcode, is_default_for_sale, is_active
        )
        values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    jdbc.update(
        sql,
        id,
        tenantId,
        productId,
        variantId,
        saleUomId,
        baseUomId,
        baseUnitsPerSaleUnit,
        barcode,
        isDefaultForSale,
        isActive
    );
    return id;
  }

  public int updatePackaging(
      UUID tenantId,
      UUID packagingId,
      UUID variantId,
      UUID saleUomId,
      UUID baseUomId,
      BigDecimal baseUnitsPerSaleUnit,
      String barcode,
      boolean isDefaultForSale,
      boolean isActive
  ) {
    String sql = """
        update product_packagings
        set variant_id = ?,
            sale_uom_id = ?,
            base_uom_id = ?,
            base_units_per_sale_unit = ?,
            barcode = ?,
            is_default_for_sale = ?,
            is_active = ?,
            updated_at = now()
        where tenant_id = ? and id = ?
        """;
    return jdbc.update(
        sql,
        variantId,
        saleUomId,
        baseUomId,
        baseUnitsPerSaleUnit,
        barcode,
        isDefaultForSale,
        isActive,
        tenantId,
        packagingId
    );
  }

  public List<PackagingLookupRecord> lookupByBarcode(UUID tenantId, String barcode, int limit) {
    String sql = """
        select pp.id as packaging_id,
               pp.product_id as product_id,
               p.name as product_name,
               pp.variant_id as variant_id,
               pv.name as variant_name,
               pp.sale_uom_id as sale_uom_id,
               su.code as sale_uom_code,
               pp.base_uom_id as base_uom_id,
               bu.code as base_uom_code,
               pp.base_units_per_sale_unit as base_units_per_sale_unit
        from product_packagings pp
        join products p on p.id = pp.product_id
        left join product_variants pv on pv.id = pp.variant_id
        join units_of_measure su on su.id = pp.sale_uom_id
        join units_of_measure bu on bu.id = pp.base_uom_id
        where pp.tenant_id = ? and pp.barcode = ?
        order by pp.id
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new PackagingLookupRecord(
            rs.getObject("packaging_id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getString("product_name"),
            rs.getObject("variant_id", UUID.class),
            rs.getString("variant_name"),
            rs.getObject("sale_uom_id", UUID.class),
            rs.getString("sale_uom_code"),
            rs.getObject("base_uom_id", UUID.class),
            rs.getString("base_uom_code"),
            rs.getBigDecimal("base_units_per_sale_unit")
        ), tenantId, barcode, limit);
  }

  public record PackagingRecord(
      UUID id,
      UUID productId,
      UUID variantId,
      UUID saleUomId,
      UUID baseUomId,
      BigDecimal baseUnitsPerSaleUnit,
      String barcode,
      boolean isDefaultForSale,
      boolean isActive
  ) {}

  public record PackagingLookupRecord(
      UUID packagingId,
      UUID productId,
      String productName,
      UUID variantId,
      String variantName,
      UUID saleUomId,
      String saleUomCode,
      UUID baseUomId,
      String baseUomCode,
      BigDecimal baseUnitsPerSaleUnit
  ) {}
}
