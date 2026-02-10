package com.sisferrete.catalog;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ProductVariantService {
  private final ProductRepository productRepository;
  private final ProductVariantRepository variantRepository;

  public ProductVariantService(
      ProductRepository productRepository,
      ProductVariantRepository variantRepository
  ) {
    this.productRepository = productRepository;
    this.variantRepository = variantRepository;
  }

  public List<VariantResponse> listVariants(UUID tenantId, UUID productId) {
    ensureProductExists(tenantId, productId);
    return variantRepository.listVariants(tenantId, productId).stream()
        .map(this::toResponse)
        .toList();
  }

  public VariantResponse createVariant(UUID tenantId, UUID productId, VariantRequest request) {
    ensureProductExists(tenantId, productId);
    String name = normalizeName(request.name());
    String barcode = normalizeOptional(request.barcode());
    boolean isActive = request.isActive() == null || request.isActive();
    Map<String, Object> attributes = request.attributes();

    if (name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre requerido");
    }
    if (barcode != null) {
      Optional<ProductVariantRepository.VariantRecord> existing =
          variantRepository.findVariantByBarcode(tenantId, barcode);
      if (existing.isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Barcode de variante ya existe");
      }
    }

    UUID id = variantRepository.insertVariant(tenantId, productId, name, barcode, attributes, isActive);
    return getVariant(tenantId, id);
  }

  public VariantResponse updateVariant(UUID tenantId, UUID variantId, VariantRequest request) {
    ProductVariantRepository.VariantRecord existing = variantRepository.findVariant(tenantId, variantId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));
    String name = normalizeName(request.name());
    String barcode = normalizeOptional(request.barcode());
    boolean isActive = request.isActive() == null || request.isActive();
    Map<String, Object> attributes = request.attributes();

    if (name.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre requerido");
    }
    if (barcode != null) {
      Optional<ProductVariantRepository.VariantRecord> byBarcode =
          variantRepository.findVariantByBarcode(tenantId, barcode);
      if (byBarcode.isPresent() && !byBarcode.get().id().equals(existing.id())) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Barcode de variante ya existe");
      }
    }

    variantRepository.updateVariant(tenantId, variantId, name, barcode, attributes, isActive);
    return getVariant(tenantId, variantId);
  }

  private VariantResponse getVariant(UUID tenantId, UUID variantId) {
    ProductVariantRepository.VariantRecord variant = variantRepository.findVariant(tenantId, variantId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Variante no encontrada"));
    return toResponse(variant);
  }

  private void ensureProductExists(UUID tenantId, UUID productId) {
    productRepository.findProduct(tenantId, productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
  }

  private String normalizeName(String name) {
    return name == null ? "" : name.trim();
  }

  private String normalizeOptional(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : trimmed;
  }

  private VariantResponse toResponse(ProductVariantRepository.VariantRecord variant) {
    return new VariantResponse(
        variant.id(),
        variant.productId(),
        variant.name(),
        variant.barcode(),
        variant.attributes(),
        variant.isActive()
    );
  }

  public record VariantRequest(
      @NotBlank String name,
      String barcode,
      Map<String, Object> attributes,
      Boolean isActive
  ) {}

  public record VariantResponse(
      UUID id,
      UUID productId,
      String name,
      String barcode,
      Map<String, Object> attributes,
      boolean isActive
  ) {}
}
