package com.sisferrete.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductService {
  private static final int DEFAULT_LIMIT = 50;
  private static final int DEFAULT_LOOKUP_LIMIT = 10;
  private final ProductRepository repository;
  private final CatalogRepository catalogRepository;
  private final ProductVariantRepository variantRepository;

  public ProductService(
      ProductRepository repository,
      CatalogRepository catalogRepository,
      ProductVariantRepository variantRepository
  ) {
    this.repository = repository;
    this.catalogRepository = catalogRepository;
    this.variantRepository = variantRepository;
  }

  public List<ProductResponse> listProducts(UUID tenantId, String query, Integer limit) {
    int safeLimit = normalizeLimit(limit, DEFAULT_LIMIT, 200);
    String normalized = normalizeSearch(query);
    return repository.searchProducts(tenantId, normalized, safeLimit).stream()
        .map(this::toResponse)
        .toList();
  }

  public ProductResponse getProduct(UUID tenantId, UUID productId) {
    ProductRepository.ProductRecord product = repository.findProduct(tenantId, productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    return toResponse(product);
  }

  public ProductResponse createProduct(UUID tenantId, ProductRequest request) {
    ProductData data = normalizeRequest(request);
    validateProductData(tenantId, data, null);
    UUID id = repository.insertProduct(
        tenantId,
        data.name(),
        data.sku(),
        data.barcode(),
        data.categoryId(),
        data.brandId(),
        data.uomId(),
        data.isActive()
    );
    return getProduct(tenantId, id);
  }

  public ProductResponse updateProduct(UUID tenantId, UUID productId, ProductRequest request) {
    repository.findProduct(tenantId, productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    ProductData data = normalizeRequest(request);
    validateProductData(tenantId, data, productId);
    repository.updateProduct(
        tenantId,
        productId,
        data.name(),
        data.sku(),
        data.barcode(),
        data.categoryId(),
        data.brandId(),
        data.uomId(),
        data.isActive()
    );
    return getProduct(tenantId, productId);
  }

  public List<LookupResponse> lookupProducts(UUID tenantId, String term, Integer limit) {
    String normalized = normalizeSearch(term);
    if (normalized == null) {
      return List.of();
    }
    int safeLimit = normalizeLimit(limit, DEFAULT_LOOKUP_LIMIT, 50);
    Map<String, LookupResponse> ordered = new LinkedHashMap<>();

    for (ProductVariantRepository.VariantLookupRecord record :
        variantRepository.lookupByBarcode(tenantId, normalized, safeLimit)) {
      String key = key(record.productId(), record.variantId());
      ordered.putIfAbsent(
          key,
          new LookupResponse(
              "VARIANT_BARCODE",
              record.productId(),
              record.productName(),
              record.variantId(),
              record.variantName(),
              record.uomId(),
              record.uomCode()
          )
      );
      if (ordered.size() >= safeLimit) {
        break;
      }
    }

    if (ordered.size() < safeLimit) {
      for (ProductRepository.ProductLookupRecord record :
          repository.lookupProductByBarcode(tenantId, normalized, safeLimit)) {
        String key = key(record.productId(), null);
        ordered.putIfAbsent(
            key,
            new LookupResponse(
                "PRODUCT_BARCODE",
                record.productId(),
                record.productName(),
                null,
                null,
                record.uomId(),
                record.uomCode()
            )
        );
        if (ordered.size() >= safeLimit) {
          break;
        }
      }
    }

    if (ordered.size() < safeLimit) {
      for (ProductRepository.ProductLookupRecord record :
          repository.lookupProductBySku(tenantId, normalized, safeLimit)) {
        String key = key(record.productId(), null);
        ordered.putIfAbsent(
            key,
            new LookupResponse(
                "PRODUCT_SKU",
                record.productId(),
                record.productName(),
                null,
                null,
                record.uomId(),
                record.uomCode()
            )
        );
        if (ordered.size() >= safeLimit) {
          break;
        }
      }
    }

    if (ordered.size() < safeLimit) {
      int remaining = safeLimit - ordered.size();
      for (ProductRepository.ProductLookupRecord record :
          repository.lookupProductByName(tenantId, normalized, remaining)) {
        String key = key(record.productId(), null);
        ordered.putIfAbsent(
            key,
            new LookupResponse(
                "PRODUCT_NAME",
                record.productId(),
                record.productName(),
                null,
                null,
                record.uomId(),
                record.uomCode()
            )
        );
        if (ordered.size() >= safeLimit) {
          break;
        }
      }
    }

    return new ArrayList<>(ordered.values());
  }

  private void validateProductData(UUID tenantId, ProductData data, UUID currentId) {
    if (data.name().isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre requerido");
    }
    if (data.uomId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UoM requerida");
    }
    if (data.sku() == null && data.barcode() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "SKU o barcode requerido");
    }
    if (!catalogRepository.existsUom(tenantId, data.uomId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UoM inválida");
    }
    if (data.categoryId() != null && !catalogRepository.existsCategory(tenantId, data.categoryId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Categoría inválida");
    }
    if (data.brandId() != null && !catalogRepository.existsBrand(tenantId, data.brandId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marca inválida");
    }
    if (data.sku() != null) {
      Optional<ProductRepository.ProductRecord> existing = repository.findProductBySku(tenantId, data.sku());
      if (existing.isPresent() && (currentId == null || !existing.get().id().equals(currentId))) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU ya existe");
      }
    }
    if (data.barcode() != null) {
      Optional<ProductRepository.ProductRecord> existing = repository.findProductByBarcode(tenantId, data.barcode());
      if (existing.isPresent() && (currentId == null || !existing.get().id().equals(currentId))) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Barcode ya existe");
      }
    }
  }

  private ProductData normalizeRequest(ProductRequest request) {
    String name = request.name() == null ? "" : request.name().trim();
    String sku = normalizeOptional(request.sku());
    String barcode = normalizeOptional(request.barcode());
    boolean isActive = request.isActive() == null || request.isActive();
    return new ProductData(
        name,
        sku,
        barcode,
        request.categoryId(),
        request.brandId(),
        request.uomId(),
        isActive
    );
  }

  private String normalizeOptional(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private String normalizeSearch(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private int normalizeLimit(Integer limit, int defaultValue, int max) {
    if (limit == null || limit <= 0) {
      return defaultValue;
    }
    return Math.min(limit, max);
  }

  private ProductResponse toResponse(ProductRepository.ProductRecord record) {
    return new ProductResponse(
        record.id(),
        record.name(),
        record.sku(),
        record.barcode(),
        record.categoryId(),
        record.brandId(),
        record.uomId(),
        record.isActive()
    );
  }

  private String key(UUID productId, UUID variantId) {
    return productId + ":" + (variantId == null ? "BASE" : variantId.toString());
  }

  private record ProductData(
      String name,
      String sku,
      String barcode,
      UUID categoryId,
      UUID brandId,
      UUID uomId,
      boolean isActive
  ) {}

  public record ProductRequest(
      @NotBlank String name,
      String sku,
      String barcode,
      UUID categoryId,
      UUID brandId,
      @NotNull UUID uomId,
      Boolean isActive
  ) {}

  public record ProductResponse(
      UUID id,
      String name,
      String sku,
      String barcode,
      UUID categoryId,
      UUID brandId,
      UUID uomId,
      boolean isActive
  ) {}

  public record LookupResponse(
      String matchType,
      UUID productId,
      String productName,
      UUID variantId,
      String variantName,
      UUID uomId,
      String uomCode
  ) {}
}
