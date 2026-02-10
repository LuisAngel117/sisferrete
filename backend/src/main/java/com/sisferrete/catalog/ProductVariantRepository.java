package com.sisferrete.catalog;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProductVariantRepository {
  private static final TypeReference<Map<String, Object>> ATTRIBUTES_TYPE = new TypeReference<>() {};
  private final JdbcTemplate jdbc;
  private final ObjectMapper objectMapper;

  public ProductVariantRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
    this.jdbc = jdbc;
    this.objectMapper = objectMapper;
  }

  public List<VariantRecord> listVariants(UUID tenantId, UUID productId) {
    String sql = """
        select id, product_id, name, barcode, attributes, is_active
        from product_variants
        where tenant_id = ? and product_id = ?
        order by name
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new VariantRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getString("name"),
            rs.getString("barcode"),
            parseAttributes(rs.getString("attributes")),
            rs.getBoolean("is_active")
        ), tenantId, productId);
  }

  public Optional<VariantRecord> findVariant(UUID tenantId, UUID variantId) {
    String sql = """
        select id, product_id, name, barcode, attributes, is_active
        from product_variants
        where tenant_id = ? and id = ?
        """;
    List<VariantRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new VariantRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getString("name"),
            rs.getString("barcode"),
            parseAttributes(rs.getString("attributes")),
            rs.getBoolean("is_active")
        ), tenantId, variantId);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public Optional<VariantRecord> findVariantByBarcode(UUID tenantId, String barcode) {
    String sql = """
        select id, product_id, name, barcode, attributes, is_active
        from product_variants
        where tenant_id = ? and barcode = ?
        """;
    List<VariantRecord> rows = jdbc.query(sql, (rs, rowNum) ->
        new VariantRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("product_id", UUID.class),
            rs.getString("name"),
            rs.getString("barcode"),
            parseAttributes(rs.getString("attributes")),
            rs.getBoolean("is_active")
        ), tenantId, barcode);
    if (rows.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(rows.get(0));
  }

  public UUID insertVariant(
      UUID tenantId,
      UUID productId,
      String name,
      String barcode,
      Map<String, Object> attributes,
      boolean isActive
  ) {
    UUID id = UUID.randomUUID();
    String sql = """
        insert into product_variants (
          id, tenant_id, product_id, name, barcode, attributes, is_active
        )
        values (?, ?, ?, ?, ?, ?::jsonb, ?)
        """;
    jdbc.update(sql, id, tenantId, productId, name, barcode, toJson(attributes), isActive);
    return id;
  }

  public int updateVariant(
      UUID tenantId,
      UUID variantId,
      String name,
      String barcode,
      Map<String, Object> attributes,
      boolean isActive
  ) {
    String sql = """
        update product_variants
        set name = ?, barcode = ?, attributes = ?::jsonb, is_active = ?, updated_at = now()
        where tenant_id = ? and id = ?
        """;
    return jdbc.update(sql, name, barcode, toJson(attributes), isActive, tenantId, variantId);
  }

  public List<VariantLookupRecord> lookupByBarcode(UUID tenantId, String barcode, int limit) {
    String sql = """
        select pv.id as variant_id,
               pv.name as variant_name,
               pv.barcode as variant_barcode,
               pv.attributes as variant_attributes,
               p.id as product_id,
               p.name as product_name,
               p.uom_id as uom_id,
               u.code as uom_code
        from product_variants pv
        join products p on p.id = pv.product_id
        join units_of_measure u on u.id = p.uom_id
        where pv.tenant_id = ?
          and pv.barcode = ?
        order by pv.name
        limit ?
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new VariantLookupRecord(
            rs.getObject("variant_id", UUID.class),
            rs.getString("variant_name"),
            rs.getString("variant_barcode"),
            parseAttributes(rs.getString("variant_attributes")),
            rs.getObject("product_id", UUID.class),
            rs.getString("product_name"),
            rs.getObject("uom_id", UUID.class),
            rs.getString("uom_code")
        ), tenantId, barcode, limit);
  }

  private Map<String, Object> parseAttributes(String raw) {
    if (raw == null || raw.isBlank()) {
      return Collections.emptyMap();
    }
    try {
      return objectMapper.readValue(raw, ATTRIBUTES_TYPE);
    } catch (Exception ex) {
      return Collections.emptyMap();
    }
  }

  private String toJson(Map<String, Object> attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return null;
    }
    try {
      return objectMapper.writeValueAsString(attributes);
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo serializar atributos", ex);
    }
  }

  public record VariantRecord(
      UUID id,
      UUID productId,
      String name,
      String barcode,
      Map<String, Object> attributes,
      boolean isActive
  ) {}

  public record VariantLookupRecord(
      UUID variantId,
      String variantName,
      String variantBarcode,
      Map<String, Object> attributes,
      UUID productId,
      String productName,
      UUID uomId,
      String uomCode
  ) {}
}
