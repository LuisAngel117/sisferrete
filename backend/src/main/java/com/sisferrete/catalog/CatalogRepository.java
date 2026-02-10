package com.sisferrete.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class CatalogRepository {
  private final JdbcTemplate jdbc;

  public CatalogRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<CategoryRecord> listCategories(UUID tenantId) {
    String sql = """
        select id, code, name, is_active
        from categories
        where tenant_id = ?
        order by code
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new CategoryRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("is_active")
        ), tenantId);
  }

  public Optional<CategoryRecord> findCategory(UUID tenantId, UUID categoryId) {
    String sql = """
        select id, code, name, is_active
        from categories
        where tenant_id = ? and id = ?
        """;
    List<CategoryRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new CategoryRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("is_active")
        ), tenantId, categoryId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<CategoryRecord> findCategoryByCode(UUID tenantId, String code) {
    String sql = """
        select id, code, name, is_active
        from categories
        where tenant_id = ? and code = ?
        """;
    List<CategoryRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new CategoryRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("is_active")
        ), tenantId, code);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public UUID insertCategory(UUID tenantId, String code, String name, boolean isActive) {
    UUID id = UUID.randomUUID();
    String sql = """
        insert into categories (id, tenant_id, code, name, is_active)
        values (?, ?, ?, ?, ?)
        """;
    jdbc.update(sql, id, tenantId, code, name, isActive);
    return id;
  }

  public int updateCategory(UUID tenantId, UUID categoryId, String code, String name, boolean isActive) {
    String sql = """
        update categories
        set code = ?, name = ?, is_active = ?, updated_at = now()
        where tenant_id = ? and id = ?
        """;
    return jdbc.update(sql, code, name, isActive, tenantId, categoryId);
  }

  public List<BrandRecord> listBrands(UUID tenantId) {
    String sql = """
        select id, code, name, is_active
        from brands
        where tenant_id = ?
        order by code
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new BrandRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("is_active")
        ), tenantId);
  }

  public Optional<BrandRecord> findBrand(UUID tenantId, UUID brandId) {
    String sql = """
        select id, code, name, is_active
        from brands
        where tenant_id = ? and id = ?
        """;
    List<BrandRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new BrandRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("is_active")
        ), tenantId, brandId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<BrandRecord> findBrandByCode(UUID tenantId, String code) {
    String sql = """
        select id, code, name, is_active
        from brands
        where tenant_id = ? and code = ?
        """;
    List<BrandRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new BrandRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("is_active")
        ), tenantId, code);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public UUID insertBrand(UUID tenantId, String code, String name, boolean isActive) {
    UUID id = UUID.randomUUID();
    String sql = """
        insert into brands (id, tenant_id, code, name, is_active)
        values (?, ?, ?, ?, ?)
        """;
    jdbc.update(sql, id, tenantId, code, name, isActive);
    return id;
  }

  public int updateBrand(UUID tenantId, UUID brandId, String code, String name, boolean isActive) {
    String sql = """
        update brands
        set code = ?, name = ?, is_active = ?, updated_at = now()
        where tenant_id = ? and id = ?
        """;
    return jdbc.update(sql, code, name, isActive, tenantId, brandId);
  }

  public List<UomRecord> listUoms(UUID tenantId) {
    String sql = """
        select id, code, name, allows_decimals, is_active
        from units_of_measure
        where tenant_id = ?
        order by code
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new UomRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("allows_decimals"),
            rs.getBoolean("is_active")
        ), tenantId);
  }

  public Optional<UomRecord> findUom(UUID tenantId, UUID uomId) {
    String sql = """
        select id, code, name, allows_decimals, is_active
        from units_of_measure
        where tenant_id = ? and id = ?
        """;
    List<UomRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new UomRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("allows_decimals"),
            rs.getBoolean("is_active")
        ), tenantId, uomId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<UomRecord> findUomByCode(UUID tenantId, String code) {
    String sql = """
        select id, code, name, allows_decimals, is_active
        from units_of_measure
        where tenant_id = ? and code = ?
        """;
    List<UomRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new UomRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getBoolean("allows_decimals"),
            rs.getBoolean("is_active")
        ), tenantId, code);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public UUID insertUom(UUID tenantId, String code, String name, boolean allowsDecimals, boolean isActive) {
    UUID id = UUID.randomUUID();
    String sql = """
        insert into units_of_measure (id, tenant_id, code, name, allows_decimals, is_active)
        values (?, ?, ?, ?, ?, ?)
        """;
    jdbc.update(sql, id, tenantId, code, name, allowsDecimals, isActive);
    return id;
  }

  public int updateUom(
      UUID tenantId,
      UUID uomId,
      String code,
      String name,
      boolean allowsDecimals,
      boolean isActive
  ) {
    String sql = """
        update units_of_measure
        set code = ?, name = ?, allows_decimals = ?, is_active = ?, updated_at = now()
        where tenant_id = ? and id = ?
        """;
    return jdbc.update(sql, code, name, allowsDecimals, isActive, tenantId, uomId);
  }

  public boolean existsCategory(UUID tenantId, UUID categoryId) {
    String sql = "select 1 from categories where tenant_id = ? and id = ?";
    return !jdbc.queryForList(sql, Integer.class, tenantId, categoryId).isEmpty();
  }

  public boolean existsBrand(UUID tenantId, UUID brandId) {
    String sql = "select 1 from brands where tenant_id = ? and id = ?";
    return !jdbc.queryForList(sql, Integer.class, tenantId, brandId).isEmpty();
  }

  public boolean existsUom(UUID tenantId, UUID uomId) {
    String sql = "select 1 from units_of_measure where tenant_id = ? and id = ?";
    return !jdbc.queryForList(sql, Integer.class, tenantId, uomId).isEmpty();
  }

  public record CategoryRecord(UUID id, String code, String name, boolean isActive) {}
  public record BrandRecord(UUID id, String code, String name, boolean isActive) {}
  public record UomRecord(UUID id, String code, String name, boolean allowsDecimals, boolean isActive) {}
}
