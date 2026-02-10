# 05 — SEGURIDAD (auth, permisos, auditoría y hardening)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Meta:** control granular, auditoría completa y protección de configuración crítica (IVA, precios, costos).  

---

## 1) Objetivos de seguridad
- Prevenir accesos no autorizados (auth fuerte).
- Prevenir abuso interno (permisos granulares + auditoría).
- Evitar fugas cross-tenant (tenant_id obligatorio en todo).
- Minimizar riesgos comunes web (OWASP básico).
- Mantener trazabilidad de acciones sensibles (antes/después).

---

## 2) Modelo de identidad y autenticación
### 2.1 Credenciales
- Login: **email + contraseña**.
- Reglas mínimas:
  - contraseña con requisitos (longitud, complejidad mínima).
  - hashing fuerte (se define al implementar; no usar plaintext).
- Recuperación de contraseña: flujo seguro (tokens expiran).

### 2.2 Sesión y tokens
- JWT + refresh token (según arquitectura).
- Tokens deben incluir:
  - `tenant_id`
  - `user_id`
  - permisos efectivos (o roles + permisos resolubles)
- Revocación:
  - refresh token rotativo (cuando se implemente).
  - invalidación en cambios sensibles (reset password, 2FA toggle, etc.)

### 2.3 2FA TOTP (opcional)
- Habilitable por tenant y por usuario.
- Recomendado para Admin/Superadmin.
- Debe existir mecanismo de recovery (códigos de respaldo o reset controlado).

---

## 3) Autorización: permisos granulares (modelo)
### 3.1 Principio
- La autorización real vive en **permisos**, no en “roles sueltos”.

### 3.2 Permisos recomendados (códigos estables)
> Estos son “mínimos”; se pueden extender.

**Configuración crítica**
- `config.iva.edit`
- `config.document_sequences.edit`
- `config.security.policies.edit`

**Usuarios y acceso**
- `users.manage`
- `roles.manage`
- `permissions.manage`
- `branches.manage`

**Precios y costos**
- `prices.edit`
- `prices.discount.manual` (aplicar descuento manual)
- `prices.sell_below_cost.authorize`
- `costs.edit`

**Inventario**
- `stock.adjust.create`
- `stock.adjust.approve`
- `stock.transfer.create`
- `stock.transfer.receive`

**Caja / POS**
- `cash.open`
- `cash.close`
- `cash.adjust` (faltantes/sobrantes)
- `sales.void` (anular)
- `sales.refund` (devolución)
- `sales.reprint` (reimprimir)

**Auditoría y reportes**
- `audit.view`
- `reports.view_sensitive` (márgenes, costos)
- `reports.export`

**E-commerce**
- `ecom.catalog.manage`
- `ecom.orders.manage`

### 3.3 Supervisor override (clave)
- Para POS: cuando un usuario sin permiso intenta:
  - descuento manual
  - venta bajo costo
  - anulaciones
- Debe pedirse credencial de supervisor o 2FA (según diseño).
- Se registra auditoría:
  - operador
  - supervisor
  - razón

---

## 4) Auditoría (obligatoria)
### 4.1 Qué auditar
Auditar TODA acción sensible y cambios en:
- IVA/configuración crítica
- precios y costos
- ajustes de inventario
- anulaciones/devoluciones
- cambios de roles/permisos
- cierres de caja

### 4.2 Campos mínimos
- fecha/hora (America/Guayaquil)
- tenant_id, branch_id (si aplica)
- actor (user_id)
- acción (código estable)
- entidad afectada (tipo + id)
- before/after (cuando aplica)
- metadata (IP, user-agent si aplica)

### 4.3 Consulta
- Solo roles/permisos autorizados pueden ver auditoría.
- Auditoría no se borra (solo archivado/retención).

---

## 5) Protección de datos y hardening
### 5.1 Secretos
- Secrets en variables de entorno (no hardcode).
- Evitar commitear `.env` con secretos.

### 5.2 CORS/CSRF
- CORS restringido por origen (cuando exista frontend).
- CSRF relevante si se usan cookies; si tokens en headers, reducir riesgo con políticas.

### 5.3 Rate limiting
- Rate limit a login y endpoints sensibles.

### 5.4 Validación y sanitización
- Validar inputs en API.
- Evitar inyección (SQL, etc.) usando prácticas seguras.

---

## 6) Reglas de “no confiar en UI”
- UI puede ocultar botones sin permiso, pero:
  - el backend SIEMPRE valida permisos.
- El backend SIEMPRE valida scoping tenant/branch.

---

## 7) Checklist de seguridad por sprint (cuando aplique)
- [ ] Endpoint valida tenant_id y branch_id cuando corresponde.
- [ ] Endpoint valida permisos para acción sensible.
- [ ] Acción sensible registra auditoría.
- [ ] No se loggean secretos.
- [ ] Errores al usuario son claros (UI ES).

<!-- EOF -->
