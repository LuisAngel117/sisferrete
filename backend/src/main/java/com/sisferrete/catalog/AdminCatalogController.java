package com.sisferrete.catalog;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/catalog")
@PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
public class AdminCatalogController {
  private final CatalogService service;

  public AdminCatalogController(CatalogService service) {
    this.service = service;
  }

  @GetMapping("/categories")
  public List<CatalogService.CategoryResponse> listCategories(JwtAuthenticationToken authentication) {
    return service.listCategories(tenantId(authentication));
  }

  @PostMapping("/categories")
  public CatalogService.CategoryResponse createCategory(
      @Valid @RequestBody CatalogService.CategoryRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.createCategory(tenantId(authentication), request);
  }

  @PutMapping("/categories/{id}")
  public CatalogService.CategoryResponse updateCategory(
      @PathVariable UUID id,
      @Valid @RequestBody CatalogService.CategoryRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.updateCategory(tenantId(authentication), id, request);
  }

  @GetMapping("/brands")
  public List<CatalogService.BrandResponse> listBrands(JwtAuthenticationToken authentication) {
    return service.listBrands(tenantId(authentication));
  }

  @PostMapping("/brands")
  public CatalogService.BrandResponse createBrand(
      @Valid @RequestBody CatalogService.BrandRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.createBrand(tenantId(authentication), request);
  }

  @PutMapping("/brands/{id}")
  public CatalogService.BrandResponse updateBrand(
      @PathVariable UUID id,
      @Valid @RequestBody CatalogService.BrandRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.updateBrand(tenantId(authentication), id, request);
  }

  @GetMapping("/uoms")
  public List<CatalogService.UomResponse> listUoms(JwtAuthenticationToken authentication) {
    return service.listUoms(tenantId(authentication));
  }

  @PostMapping("/uoms")
  public CatalogService.UomResponse createUom(
      @Valid @RequestBody CatalogService.UomRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.createUom(tenantId(authentication), request);
  }

  @PutMapping("/uoms/{id}")
  public CatalogService.UomResponse updateUom(
      @PathVariable UUID id,
      @Valid @RequestBody CatalogService.UomRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.updateUom(tenantId(authentication), id, request);
  }

  private UUID tenantId(JwtAuthenticationToken authentication) {
    String value = authentication.getToken().getClaimAsString("tenant_id");
    if (value == null || value.isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant no encontrado");
    }
    try {
      return UUID.fromString(value);
    } catch (IllegalArgumentException ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Tenant inválido");
    }
  }
}
