package com.sisferrete.catalog;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PackagingService {
  private final PackagingRepository repository;
  private final ProductRepository productRepository;
  private final ProductVariantRepository variantRepository;
  private final CatalogRepository catalogRepository;

  public PackagingService(
      PackagingRepository repository,
      ProductRepository productRepository,
      ProductVariantRepository variantRepository,
      CatalogRepository catalogRepository
  ) {
    this.repository = repository;
    this.productRepository = productRepository;
    this.variantRepository = variantRepository;
    this.catalogRepository = catalogRepository;
  }

  public List<PackagingResponse> listPackagings(UUID tenantId, UUID productId) {
    ensureProductExists(tenantId, productId);
    return repository.listPackagings(tenantId, productId).stream()
        .map(this::toResponse)
        .toList();
  }

  public PackagingResponse createPackaging(UUID tenantId, UUID productId, PackagingRequest request) {
    ProductRepository.ProductRecord product = getProduct(tenantId, productId);
    PackagingData data = normalizeRequest(request);
    validatePackagingData(tenantId, product, data, null);
    UUID id = repository.insertPackaging(
        tenantId,
        productId,
        data.variantId(),
        data.saleUomId(),
        data.baseUomId(),
        data.baseUnitsPerSaleUnit(),
        data.barcode(),
        data.isDefaultForSale(),
        data.isActive()
    );
    return getPackaging(tenantId, id);
  }

  public PackagingResponse updatePackaging(UUID tenantId, UUID packagingId, PackagingRequest request) {
    PackagingRepository.PackagingRecord existing = repository.findPackaging(tenantId, packagingId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Presentación no encontrada"));
    ProductRepository.ProductRecord product = getProduct(tenantId, existing.productId());
    PackagingData data = normalizeRequest(request);
    validatePackagingData(tenantId, product, data, packagingId);
    repository.updatePackaging(
        tenantId,
        packagingId,
        data.variantId(),
        data.saleUomId(),
        data.baseUomId(),
        data.baseUnitsPerSaleUnit(),
        data.barcode(),
        data.isDefaultForSale(),
        data.isActive()
    );
    return getPackaging(tenantId, packagingId);
  }

  private PackagingResponse getPackaging(UUID tenantId, UUID packagingId) {
    PackagingRepository.PackagingRecord record = repository.findPackaging(tenantId, packagingId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Presentación no encontrada"));
    return toResponse(record);
  }

  private void validatePackagingData(
      UUID tenantId,
      ProductRepository.ProductRecord product,
      PackagingData data,
      UUID currentId
  ) {
    if (data.saleUomId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UoM de venta requerida");
    }
    if (data.baseUomId() == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UoM base requerida");
    }
    if (data.baseUnitsPerSaleUnit() == null || data.baseUnitsPerSaleUnit().compareTo(BigDecimal.ZERO) <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Factor inválido");
    }
    if (!catalogRepository.existsUom(tenantId, data.saleUomId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UoM de venta inválida");
    }
    if (!catalogRepository.existsUom(tenantId, data.baseUomId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UoM base inválida");
    }
    if (!product.uomId().equals(data.baseUomId())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "UoM base debe coincidir con el producto");
    }
    if (data.variantId() != null) {
      ProductVariantRepository.VariantRecord variant = variantRepository.findVariant(tenantId, data.variantId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variante inválida"));
      if (!variant.productId().equals(product.id())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Variante no pertenece al producto");
      }
    }
    if (data.barcode() != null) {
      Optional<PackagingRepository.PackagingRecord> byBarcode =
          repository.findPackagingByBarcode(tenantId, data.barcode());
      if (byBarcode.isPresent() && (currentId == null || !byBarcode.get().id().equals(currentId))) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Barcode de presentación ya existe");
      }
    }
    Optional<PackagingRepository.PackagingRecord> byComposite =
        repository.findPackagingByComposite(tenantId, product.id(), data.variantId(), data.saleUomId());
    if (byComposite.isPresent() && (currentId == null || !byComposite.get().id().equals(currentId))) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Presentación duplicada");
    }
  }

  private PackagingData normalizeRequest(PackagingRequest request) {
    String barcode = normalizeOptional(request.barcode());
    boolean isDefaultForSale = request.isDefaultForSale() != null && request.isDefaultForSale();
    boolean isActive = request.isActive() == null || request.isActive();
    return new PackagingData(
        request.variantId(),
        request.saleUomId(),
        request.baseUomId(),
        request.baseUnitsPerSaleUnit(),
        barcode,
        isDefaultForSale,
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

  private ProductRepository.ProductRecord getProduct(UUID tenantId, UUID productId) {
    return productRepository.findProduct(tenantId, productId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
  }

  private void ensureProductExists(UUID tenantId, UUID productId) {
    getProduct(tenantId, productId);
  }

  private PackagingResponse toResponse(PackagingRepository.PackagingRecord record) {
    return new PackagingResponse(
        record.id(),
        record.productId(),
        record.variantId(),
        record.saleUomId(),
        record.baseUomId(),
        record.baseUnitsPerSaleUnit(),
        record.barcode(),
        record.isDefaultForSale(),
        record.isActive()
    );
  }

  private record PackagingData(
      UUID variantId,
      UUID saleUomId,
      UUID baseUomId,
      BigDecimal baseUnitsPerSaleUnit,
      String barcode,
      boolean isDefaultForSale,
      boolean isActive
  ) {}

  public record PackagingRequest(
      UUID variantId,
      @NotNull UUID saleUomId,
      @NotNull UUID baseUomId,
      @NotNull BigDecimal baseUnitsPerSaleUnit,
      String barcode,
      Boolean isDefaultForSale,
      Boolean isActive
  ) {}

  public record PackagingResponse(
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
}
