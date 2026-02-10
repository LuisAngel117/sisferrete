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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasAuthority('PERM_CATALOG_MANAGE')")
public class AdminProductsController {
  private final ProductService service;

  public AdminProductsController(ProductService service) {
    this.service = service;
  }

  @GetMapping
  public List<ProductService.ProductResponse> listProducts(
      @RequestParam(name = "query", required = false) String query,
      @RequestParam(name = "limit", required = false) Integer limit,
      JwtAuthenticationToken authentication
  ) {
    return service.listProducts(tenantId(authentication), query, limit);
  }

  @GetMapping("/{id}")
  public ProductService.ProductResponse getProduct(
      @PathVariable UUID id,
      JwtAuthenticationToken authentication
  ) {
    return service.getProduct(tenantId(authentication), id);
  }

  @PostMapping
  public ProductService.ProductResponse createProduct(
      @Valid @RequestBody ProductService.ProductRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.createProduct(tenantId(authentication), request);
  }

  @PutMapping("/{id}")
  public ProductService.ProductResponse updateProduct(
      @PathVariable UUID id,
      @Valid @RequestBody ProductService.ProductRequest request,
      JwtAuthenticationToken authentication
  ) {
    return service.updateProduct(tenantId(authentication), id, request);
  }

  @GetMapping("/lookup")
  public List<ProductService.ProductResponse> lookupProducts(
      @RequestParam(name = "term") String term,
      @RequestParam(name = "limit", required = false) Integer limit,
      JwtAuthenticationToken authentication
  ) {
    return service.lookupProducts(tenantId(authentication), term, limit);
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
