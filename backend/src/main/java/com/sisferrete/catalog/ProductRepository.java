package com.sisferrete.catalog;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductRepository {
  private final JdbcTemplate jdbc;

  public ProductRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<ProductRecord> searchProducts(UUID tenantId, String query, int limit) {
    if (query == null || query.isBlank()) {
      String sql = """
          select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
          from products
          where tenant_id = ?
          order by name
          limit ?
          """;
      return jdbc.query(sql, (rs, rowNum) ->
          new ProductRecord(
              rs.getObject("id", UUID.class),
              rs.getString("name"),
              rs.getString("sku"),
              rs.getString("barcode"),
              rs.getObject("category_id", UUID.class),
              rs.getObject("brand_id", UUID.class),
              rs.getObject("uom_id", UUID.class),
              rs.getBoolean("is_active"),
              rs.getObject("tenant_id", UUID.class)
          ), tenantId, limit);
    }

    String like = "%" + query + "%";
    String sql = """
        select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
        from products
        where tenant_id = ?
          and (
            name ilike ?
            or sku ilike ?
            or barcode = ?
          )
        order by name
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new ProductRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("sku"),
            rs.getString("barcode"),
            rs.getObject("category_id", UUID.class),
            rs.getObject("brand_id", UUID.class),
            rs.getObject("uom_id", UUID.class),
            rs.getBoolean("is_active"),
            rs.getObject("tenant_id", UUID.class)
        ), tenantId, like, like, query, limit);
  }

  public Optional<ProductRecord> findProduct(UUID tenantId, UUID productId) {
    String sql = """
        select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
        from products
        where tenant_id = ? and id = ?
        """;
    List<ProductRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new ProductRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("sku"),
            rs.getString("barcode"),
            rs.getObject("category_id", UUID.class),
            rs.getObject("brand_id", UUID.class),
            rs.getObject("uom_id", UUID.class),
            rs.getBoolean("is_active"),
            rs.getObject("tenant_id", UUID.class)
        ), tenantId, productId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<ProductRecord> findProductById(UUID productId) {
    String sql = """
        select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
        from products
        where id = ?
        """;
    List<ProductRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new ProductRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("sku"),
            rs.getString("barcode"),
            rs.getObject("category_id", UUID.class),
            rs.getObject("brand_id", UUID.class),
            rs.getObject("uom_id", UUID.class),
            rs.getBoolean("is_active"),
            rs.getObject("tenant_id", UUID.class)
        ), productId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<ProductRecord> findProductBySku(UUID tenantId, String sku) {
    String sql = """
        select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
        from products
        where tenant_id = ? and sku = ?
        """;
    List<ProductRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new ProductRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("sku"),
            rs.getString("barcode"),
            rs.getObject("category_id", UUID.class),
            rs.getObject("brand_id", UUID.class),
            rs.getObject("uom_id", UUID.class),
            rs.getBoolean("is_active"),
            rs.getObject("tenant_id", UUID.class)
        ), tenantId, sku);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<ProductRecord> findProductByBarcode(UUID tenantId, String barcode) {
    String sql = """
        select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
        from products
        where tenant_id = ? and barcode = ?
        """;
    List<ProductRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new ProductRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("sku"),
            rs.getString("barcode"),
            rs.getObject("category_id", UUID.class),
            rs.getObject("brand_id", UUID.class),
            rs.getObject("uom_id", UUID.class),
            rs.getBoolean("is_active"),
            rs.getObject("tenant_id", UUID.class)
        ), tenantId, barcode);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public UUID insertProduct(
      UUID tenantId,
      String name,
      String sku,
      String barcode,
      UUID categoryId,
      UUID brandId,
      UUID uomId,
      boolean isActive
  ) {
    UUID id = UUID.randomUUID();
    String sql = """
        insert into products (
          id, tenant_id, name, sku, barcode, category_id, brand_id, uom_id, is_active
        )
        values (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
    jdbc.update(sql, id, tenantId, name, sku, barcode, categoryId, brandId, uomId, isActive);
    return id;
  }

  public int updateProduct(
      UUID tenantId,
      UUID productId,
      String name,
      String sku,
      String barcode,
      UUID categoryId,
      UUID brandId,
      UUID uomId,
      boolean isActive
  ) {
    String sql = """
        update products
        set name = ?, sku = ?, barcode = ?, category_id = ?, brand_id = ?, uom_id = ?, is_active = ?, updated_at = now()
        where tenant_id = ? and id = ?
        """;
    return jdbc.update(sql, name, sku, barcode, categoryId, brandId, uomId, isActive, tenantId, productId);
  }

  public List<ProductRecord> lookupByBarcode(UUID tenantId, String barcode, int limit) {
    String sql = """
        select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
        from products
        where tenant_id = ? and barcode = ?
        order by name
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new ProductRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("sku"),
            rs.getString("barcode"),
            rs.getObject("category_id", UUID.class),
            rs.getObject("brand_id", UUID.class),
            rs.getObject("uom_id", UUID.class),
            rs.getBoolean("is_active"),
            rs.getObject("tenant_id", UUID.class)
        ), tenantId, barcode, limit);
  }

  public List<ProductRecord> lookupBySku(UUID tenantId, String sku, int limit) {
    String sql = """
        select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
        from products
        where tenant_id = ? and sku = ?
        order by name
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new ProductRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("sku"),
            rs.getString("barcode"),
            rs.getObject("category_id", UUID.class),
            rs.getObject("brand_id", UUID.class),
            rs.getObject("uom_id", UUID.class),
            rs.getBoolean("is_active"),
            rs.getObject("tenant_id", UUID.class)
        ), tenantId, sku, limit);
  }

  public List<ProductRecord> lookupByName(UUID tenantId, String term, int limit) {
    String like = "%" + term + "%";
    String sql = """
        select id, name, sku, barcode, category_id, brand_id, uom_id, is_active, tenant_id
        from products
        where tenant_id = ? and name ilike ?
        order by name
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new ProductRecord(
            rs.getObject("id", UUID.class),
            rs.getString("name"),
            rs.getString("sku"),
            rs.getString("barcode"),
            rs.getObject("category_id", UUID.class),
            rs.getObject("brand_id", UUID.class),
            rs.getObject("uom_id", UUID.class),
            rs.getBoolean("is_active"),
            rs.getObject("tenant_id", UUID.class)
        ), tenantId, like, limit);
  }

  public List<ProductLookupRecord> lookupProductByBarcode(UUID tenantId, String barcode, int limit) {
    String sql = """
        select p.id as product_id,
               p.name as product_name,
               p.uom_id as uom_id,
               u.code as uom_code
        from products p
        join units_of_measure u on u.id = p.uom_id
        where p.tenant_id = ? and p.barcode = ?
        order by p.name
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new ProductLookupRecord(
            rs.getObject("product_id", UUID.class),
            rs.getString("product_name"),
            rs.getObject("uom_id", UUID.class),
            rs.getString("uom_code")
        ), tenantId, barcode, limit);
  }

  public List<ProductLookupRecord> lookupProductBySku(UUID tenantId, String sku, int limit) {
    String sql = """
        select p.id as product_id,
               p.name as product_name,
               p.uom_id as uom_id,
               u.code as uom_code
        from products p
        join units_of_measure u on u.id = p.uom_id
        where p.tenant_id = ? and p.sku = ?
        order by p.name
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new ProductLookupRecord(
            rs.getObject("product_id", UUID.class),
            rs.getString("product_name"),
            rs.getObject("uom_id", UUID.class),
            rs.getString("uom_code")
        ), tenantId, sku, limit);
  }

  public List<ProductLookupRecord> lookupProductByName(UUID tenantId, String term, int limit) {
    String like = "%" + term + "%";
    String sql = """
        select p.id as product_id,
               p.name as product_name,
               p.uom_id as uom_id,
               u.code as uom_code
        from products p
        join units_of_measure u on u.id = p.uom_id
        where p.tenant_id = ? and p.name ilike ?
        order by p.name
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new ProductLookupRecord(
            rs.getObject("product_id", UUID.class),
            rs.getString("product_name"),
            rs.getObject("uom_id", UUID.class),
            rs.getString("uom_code")
        ), tenantId, like, limit);
  }

  public record ProductRecord(
      UUID id,
      String name,
      String sku,
      String barcode,
      UUID categoryId,
      UUID brandId,
      UUID uomId,
      boolean isActive,
      UUID tenantId
  ) {}

  public record ProductLookupRecord(
      UUID productId,
      String productName,
      UUID uomId,
      String uomCode
  ) {}
}
