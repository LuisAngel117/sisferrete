package com.sisferrete.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CatalogService {
  private static final java.util.regex.Pattern CODE_PATTERN = java.util.regex.Pattern.compile("^[A-Z0-9_]+$");
  private final CatalogRepository repository;

  public CatalogService(CatalogRepository repository) {
    this.repository = repository;
  }

  public List<CategoryResponse> listCategories(UUID tenantId) {
    return repository.listCategories(tenantId).stream()
        .map(this::toCategoryResponse)
        .toList();
  }

  public CategoryResponse createCategory(UUID tenantId, CategoryRequest request) {
    String code = normalizeCode(request.code());
    String name = normalizeName(request.name());
    boolean isActive = Boolean.TRUE.equals(request.isActive());
    ensureValidCode(code);
    ensureCategoryCodeAvailable(tenantId, code, null);
    UUID id = repository.insertCategory(tenantId, code, name, isActive);
    return getCategory(tenantId, id);
  }

  public CategoryResponse updateCategory(UUID tenantId, UUID categoryId, CategoryRequest request) {
    String code = normalizeCode(request.code());
    String name = normalizeName(request.name());
    boolean isActive = Boolean.TRUE.equals(request.isActive());
    ensureValidCode(code);
    CatalogRepository.CategoryRecord existing = repository.findCategory(tenantId, categoryId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    ensureCategoryCodeAvailable(tenantId, code, existing.id());
    repository.updateCategory(tenantId, categoryId, code, name, isActive);
    return getCategory(tenantId, categoryId);
  }

  public List<BrandResponse> listBrands(UUID tenantId) {
    return repository.listBrands(tenantId).stream()
        .map(this::toBrandResponse)
        .toList();
  }

  public BrandResponse createBrand(UUID tenantId, BrandRequest request) {
    String code = normalizeCode(request.code());
    String name = normalizeName(request.name());
    boolean isActive = Boolean.TRUE.equals(request.isActive());
    ensureValidCode(code);
    ensureBrandCodeAvailable(tenantId, code, null);
    UUID id = repository.insertBrand(tenantId, code, name, isActive);
    return getBrand(tenantId, id);
  }

  public BrandResponse updateBrand(UUID tenantId, UUID brandId, BrandRequest request) {
    String code = normalizeCode(request.code());
    String name = normalizeName(request.name());
    boolean isActive = Boolean.TRUE.equals(request.isActive());
    ensureValidCode(code);
    CatalogRepository.BrandRecord existing = repository.findBrand(tenantId, brandId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
    ensureBrandCodeAvailable(tenantId, code, existing.id());
    repository.updateBrand(tenantId, brandId, code, name, isActive);
    return getBrand(tenantId, brandId);
  }

  public List<UomResponse> listUoms(UUID tenantId) {
    return repository.listUoms(tenantId).stream()
        .map(this::toUomResponse)
        .toList();
  }

  public UomResponse createUom(UUID tenantId, UomRequest request) {
    String code = normalizeCode(request.code());
    String name = normalizeName(request.name());
    boolean isActive = Boolean.TRUE.equals(request.isActive());
    boolean allowsDecimals = Boolean.TRUE.equals(request.allowsDecimals());
    ensureValidCode(code);
    ensureUomCodeAvailable(tenantId, code, null);
    UUID id = repository.insertUom(tenantId, code, name, allowsDecimals, isActive);
    return getUom(tenantId, id);
  }

  public UomResponse updateUom(UUID tenantId, UUID uomId, UomRequest request) {
    String code = normalizeCode(request.code());
    String name = normalizeName(request.name());
    boolean isActive = Boolean.TRUE.equals(request.isActive());
    boolean allowsDecimals = Boolean.TRUE.equals(request.allowsDecimals());
    ensureValidCode(code);
    CatalogRepository.UomRecord existing = repository.findUom(tenantId, uomId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
    ensureUomCodeAvailable(tenantId, code, existing.id());
    repository.updateUom(tenantId, uomId, code, name, allowsDecimals, isActive);
    return getUom(tenantId, uomId);
  }

  private CategoryResponse getCategory(UUID tenantId, UUID categoryId) {
    CatalogRepository.CategoryRecord category = repository.findCategory(tenantId, categoryId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Categoría no encontrada"));
    return toCategoryResponse(category);
  }

  private BrandResponse getBrand(UUID tenantId, UUID brandId) {
    CatalogRepository.BrandRecord brand = repository.findBrand(tenantId, brandId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Marca no encontrada"));
    return toBrandResponse(brand);
  }

  private UomResponse getUom(UUID tenantId, UUID uomId) {
    CatalogRepository.UomRecord uom = repository.findUom(tenantId, uomId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unidad no encontrada"));
    return toUomResponse(uom);
  }

  private void ensureCategoryCodeAvailable(UUID tenantId, String code, UUID currentId) {
    Optional<CatalogRepository.CategoryRecord> existing = repository.findCategoryByCode(tenantId, code);
    if (existing.isPresent() && (currentId == null || !existing.get().id().equals(currentId))) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de categoría ya existe");
    }
  }

  private void ensureBrandCodeAvailable(UUID tenantId, String code, UUID currentId) {
    Optional<CatalogRepository.BrandRecord> existing = repository.findBrandByCode(tenantId, code);
    if (existing.isPresent() && (currentId == null || !existing.get().id().equals(currentId))) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de marca ya existe");
    }
  }

  private void ensureUomCodeAvailable(UUID tenantId, String code, UUID currentId) {
    Optional<CatalogRepository.UomRecord> existing = repository.findUomByCode(tenantId, code);
    if (existing.isPresent() && (currentId == null || !existing.get().id().equals(currentId))) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de unidad ya existe");
    }
  }

  private void ensureValidCode(String code) {
    if (code == null || !CODE_PATTERN.matcher(code).matches()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Código inválido. Usa A-Z, 0-9 y _");
    }
  }

  private String normalizeCode(String code) {
    return code == null ? "" : code.trim();
  }

  private String normalizeName(String name) {
    return name == null ? "" : name.trim();
  }

  private CategoryResponse toCategoryResponse(CatalogRepository.CategoryRecord category) {
    return new CategoryResponse(category.id(), category.code(), category.name(), category.isActive());
  }

  private BrandResponse toBrandResponse(CatalogRepository.BrandRecord brand) {
    return new BrandResponse(brand.id(), brand.code(), brand.name(), brand.isActive());
  }

  private UomResponse toUomResponse(CatalogRepository.UomRecord uom) {
    return new UomResponse(uom.id(), uom.code(), uom.name(), uom.allowsDecimals(), uom.isActive());
  }

  public record CategoryRequest(
      @NotBlank @Pattern(regexp = "^[A-Z0-9_]+$") String code,
      @NotBlank String name,
      @NotNull Boolean isActive
  ) {}

  public record BrandRequest(
      @NotBlank @Pattern(regexp = "^[A-Z0-9_]+$") String code,
      @NotBlank String name,
      @NotNull Boolean isActive
  ) {}

  public record UomRequest(
      @NotBlank @Pattern(regexp = "^[A-Z0-9_]+$") String code,
      @NotBlank String name,
      @NotNull Boolean allowsDecimals,
      @NotNull Boolean isActive
  ) {}

  public record CategoryResponse(UUID id, String code, String name, boolean isActive) {}
  public record BrandResponse(UUID id, String code, String name, boolean isActive) {}
  public record UomResponse(UUID id, String code, String name, boolean allowsDecimals, boolean isActive) {}
}
