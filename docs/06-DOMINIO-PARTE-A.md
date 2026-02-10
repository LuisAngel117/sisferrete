# 06 — DOMINIO (PARTE A) — Identidad, sucursales, catálogo y precios
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Nota:** Este dominio es SaaS-ready: `tenant_id` existe desde el inicio.  

---

## 1) Convenciones de modelado
- Todas las entidades de negocio incluyen:
  - `id` (UUID recomendado)
  - `tenant_id` (UUID)
  - `created_at`, `updated_at`
  - `created_by` (user_id) cuando aplique
  - `is_active` (o estado equivalente) en vez de borrar
- `branch_id` se usa donde el dato sea específico de sucursal.
- Las referencias a dinero usan decimal exacto; cantidades usan decimal exacto según UoM.

---

## 2) Núcleo multi-tenant
### 2.1 Tenant
**Tabla:** `tenants`  
**Campos:**
- `id` (uuid)
- `name` (string) — ejemplo seed: “Ferreteria”
- `legal_name` (string, nullable)
- `tax_id` (string, nullable)
- `timezone` (string) — default: `America/Guayaquil`
- `currency` (string) — default: `USD`
- `settings_json` (jsonb) — flags y configuración avanzada
- `is_active` (bool)

**Reglas:**
- Un usuario pertenece a un tenant.
- Todo dato operativo debe estar atado a tenant.

### 2.2 Branch (Sucursal)
**Tabla:** `branches`  
**Campos:**
- `id` (uuid)
- `tenant_id` (uuid)
- `name` (string)
- `code` (string, unique por tenant)
- `address` (string)
- `phone` (string, nullable)
- `is_active` (bool)

**Reglas:**
- Muchas operaciones se registran por branch (ventas, stock, caja).

---

## 3) Usuarios, roles, permisos
### 3.1 User
**Tabla:** `users`  
**Campos:**
- `id` (uuid)
- `tenant_id` (uuid)
- `email` (string, unique por tenant)
- `password_hash` (string)
- `full_name` (string)
- `is_active` (bool)
- `two_factor_enabled` (bool)
- `two_factor_secret` (string, nullable, protegido)
- `last_login_at` (timestamp, nullable)

### 3.2 Role
**Tabla:** `roles`  
**Campos:**
- `id` (uuid)
- `tenant_id` (uuid)
- `code` (string, unique por tenant) — ejemplo: `ADMIN`, `CAJERO`
- `name` (string)
- `description` (string, nullable)
- `is_active` (bool)

### 3.3 Permission
**Tabla:** `permissions`  
**Campos:**
- `id` (uuid)
- `code` (string, unique global o por tenant; recomendado global estable)
- `name` (string)
- `description` (string)

### 3.4 RolePermission
**Tabla:** `role_permissions`  
**Campos:**
- `role_id`
- `permission_id`

### 3.5 UserRoleAssignment
**Tabla:** `user_roles`  
**Campos:**
- `id` (uuid)
- `tenant_id` (uuid)
- `user_id` (uuid)
- `role_id` (uuid)
- `scope_branch_id` (uuid, nullable)
  - null = aplica a todo el tenant
  - set = aplica solo a una sucursal

**Reglas:**
- Permite roles por sucursal (útil para cajeros por branch).

### 3.6 UserBranchAccess (opcional)
**Tabla:** `user_branch_access`  
**Campos:**
- `user_id`, `branch_id`

**Regla:**
- Define a qué sucursales puede operar un usuario.

---

## 4) Auditoría
### 4.1 AuditEvent
**Tabla:** `audit_events`  
**Campos:**
- `id` (uuid)
- `tenant_id` (uuid)
- `branch_id` (uuid, nullable)
- `actor_user_id` (uuid)
- `action_code` (string) — ej: `stock.adjust.create`
- `entity_type` (string) — ej: `Product`, `Sale`
- `entity_id` (uuid, nullable)
- `before_json` (jsonb, nullable)
- `after_json` (jsonb, nullable)
- `metadata_json` (jsonb) — ip, ua, notas, supervisor override
- `created_at` (timestamp)

---

## 5) Catálogo
### 5.1 Category
**Tabla:** `categories`  
**Campos:** `id`, `tenant_id`, `name`, `parent_id` (nullable), `is_active`

### 5.2 Brand
**Tabla:** `brands`  
**Campos:** `id`, `tenant_id`, `name`, `is_active`

### 5.3 UnitOfMeasure (UoM)
**Tabla:** `uoms`  
**Campos:**
- `id` (uuid)
- `tenant_id` (uuid)
- `code` (string) — `UNIDAD`, `METRO`, `KILO`, `LITRO`
- `name` (string)
- `allows_decimal` (bool)
- `decimal_scale` (int) — ej: 3 para metro/kilo/litro
- `is_active` (bool)

**Regla:**
- `allows_decimal` define validación de cantidades.

### 5.4 Product
**Tabla:** `products`  
**Campos:**
- `id`, `tenant_id`
- `sku` (string, nullable, unique por tenant)
- `barcode` (string, nullable, index)
- `name` (string)
- `short_description` (string, nullable)
- `category_id` (uuid)
- `brand_id` (uuid, nullable)
- `base_uom_id` (uuid)
- `manages_serials` (bool)
- `manages_batches` (bool)
- `is_kit` (bool)
- `is_active` (bool)

**Reglas:**
- SKU y/o barcode pueden existir; al menos uno recomendado para venta rápida.
- `is_active=false` desactiva en POS/e-commerce sin borrar histórico.

### 5.5 ProductVariant
**Tabla:** `product_variants`  
**Campos:**
- `id`, `tenant_id`
- `product_id`
- `variant_code` (string, nullable)
- `attributes_json` (jsonb) — medida, color, calibre, voltaje, etc.
- `barcode` (string, nullable) — barcode por variante (recomendado)
- `is_active` (bool)

### 5.6 ProductPackaging (unidad vs caja/paquete)
**Tabla:** `product_packaging`  
**Campos:**
- `id`, `tenant_id`
- `product_id` (o variant_id)
- `name` (string) — “Caja x 100”
- `units_per_pack` (decimal exacto; normalmente entero)
- `is_default` (bool)

**Reglas:**
- Permite vender “1 caja” y descontar unidades equivalentes.

### 5.7 ProductKitComponent
**Tabla:** `product_kit_components`  
**Campos:**
- `kit_product_id` (product_id)
- `component_product_id` (product_id o variant_id)
- `quantity` (decimal)
- `uom_id` (uuid)

**Reglas:**
- Vender kit descuenta componentes.
- No permitir stock negativo por descuento de componentes.

### 5.8 ProductImage (para UI/e-commerce)
**Tabla:** `product_images`  
**Campos:**
- `id`, `tenant_id`
- `product_id` (o variant_id)
- `url` (string)
- `sort_order` (int)

> Storage/hosting se define después; por ahora es contrato de datos.

---

## 6) Precios
### 6.1 PriceList
**Tabla:** `price_lists`  
**Campos:**
- `id`, `tenant_id`
- `code` (string) — `RETAIL`, `MAYORISTA`
- `name` (string)
- `currency` (string) — USD
- `is_active` (bool)

### 6.2 PriceEntry
**Tabla:** `price_entries`  
**Campos:**
- `id`, `tenant_id`
- `price_list_id`
- `product_id` / `variant_id`
- `packaging_id` (nullable)
- `price` (decimal, 2)
- `valid_from` (nullable), `valid_to` (nullable)
- `created_by`, `created_at`

### 6.3 Campaign (descuentos por campaña)
**Tabla:** `campaigns`  
**Campos:**
- `id`, `tenant_id`
- `name`
- `start_at`, `end_at`
- `is_active`

### 6.4 CampaignRule
**Tabla:** `campaign_rules`  
**Campos:**
- `id`, `tenant_id`
- `campaign_id`
- target (category/brand/product/variant)
- `discount_type` (percent/fixed)
- `discount_value` (decimal)

---

## 7) Configuración crítica (IVA, reglas)
### 7.1 TenantConfig (sugerido)
**Tabla:** `tenant_config`  
**Campos:**
- `tenant_id` (pk)
- `vat_rate` (decimal) — editable, ultra protegido
- `rounding_mode` (string)
- `sell_below_cost_policy` (jsonb)
- `updated_by`, `updated_at`

**Regla:**
- Cambios a IVA siempre auditar, y requieren permiso `config.iva.edit`.

<!-- EOF -->
