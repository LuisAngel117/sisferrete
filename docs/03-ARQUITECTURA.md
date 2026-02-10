# 03 — ARQUITECTURA (visión técnica + decisiones base)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Meta:** arquitectura estable para no rehacer el core cuando se agregue e-commerce y/o módulo SRI.  

---

## 1) Objetivos arquitectónicos
- **Evitar desviación:** reglas claras, módulos definidos, cambios controlados.
- **POS-first:** latencia baja y operaciones consistentes.
- **SaaS-ready:** tenant_id desde el inicio (sin “migración dolorosa”).
- **Local-first:** correr y probar en local con Postgres instalable.
- **Evolutivo:** permitir e-commerce desde el inicio y fiscal SRI después.
- **Seguridad:** permisos granulares + auditoría.

---

## 2) Stack (sin amarrar versiones rígidas)
- Backend: Java 21 + Spring Boot 3.x
- DB: PostgreSQL (local instalable) + migraciones
- Frontend: React + Vite + TypeScript + Tailwind + shadcn/ui
- Auth: JWT + refresh token; 2FA TOTP opcional (Admin/Superadmin)
- Calidad: formatter + linter + reglas de naming

> Nota: Las versiones exactas se documentarán en ADR cuando existan los archivos del proyecto.

---

## 3) Estructura del repo (monorepo)
**Propuesta (se materializa en sprints posteriores):**
- `/backend` — API Spring Boot
- `/frontend` — App React (admin/POS + e-commerce público)
- `/docs` — fuente de verdad
- `/scripts` — verificadores locales (docs, smoke, etc.)

---

## 4) Estilo arquitectónico: “modular monolith” (backend)
**Decisión:** iniciar con monolito modular bien separado por dominios (evita microservicios prematuros).

### 4.1 Capas recomendadas
- **api (web)**: controllers, request/response DTOs, validaciones de entrada
- **application**: casos de uso (servicios de aplicación), transacciones, orquestación
- **domain**: entidades, value objects, reglas de negocio puras
- **infrastructure**: persistencia, repositorios, integraciones, storage, mensajería futura

**Regla:** el dominio NO depende de infraestructura.

---

## 5) Multi-tenant (SaaS-ready) desde el inicio
### 5.1 Modelo
- `tenant_id` es obligatorio en todas las entidades de negocio.
- `branch_id` (sucursal) aplica donde corresponde (ventas, stock, caja, etc.).

### 5.2 Propagación del contexto (request → dominio)
**Propuesta base:**
- JWT incluye `tenant_id` y permisos.
- `branch_id` se define por:
  - selección activa del usuario (UI) y/o
  - header `X-Branch-Id` en requests (para endpoints scope sucursal).
- Backend valida:
  - usuario pertenece al tenant
  - acceso a sucursal (si aplica)
  - permisos

### 5.3 Enforcements (anti “data leak”)
- En repositorios/queries: siempre filtrar por `tenant_id` (y `branch_id` cuando aplique).
- Validación adicional:
  - tests de scoping (sin tenant/branch debe fallar)
- Auditoría registra `tenant_id` y `branch_id`.

---

## 6) Seguridad: permisos granulares (modelo)
### 6.1 RBAC con permisos como “primitivas”
- Roles = paquetes (ADMIN, CAJERO, etc.)
- Permisos = acciones concretas (strings/códigos estables)
- Un usuario puede tener múltiples roles (o roles por sucursal) según definición de dominio.

### 6.2 Autorización en operaciones sensibles
Ejemplos:
- descuento manual
- venta bajo costo
- ajuste stock
- cambio precio/costo
- editar IVA global

**Requisito técnico:** la autorización debe ser chequeada en capa application (caso de uso) y reforzada en api (guards).

### 6.3 2FA TOTP
- Opcional y configurable.
- Aplicable mínimo a Admin/Superadmin.
- Se define flujo UX en docs/07-UX-UI.

---

## 7) Datos: PostgreSQL + migraciones
### 7.1 Migraciones
Para evitar ambigüedad, esta arquitectura asume **Flyway** como default (si se cambia, se hace por ADR).
- Migrations versionadas en repo.
- Semillas demo (tenant “Ferreteria”) para local.

### 7.2 Tipos de datos críticos
- Dinero: decimal exacto con 2 decimales.
- Cantidades:
  - decimales permitidos por UoM
  - escala definida (ej: 3 decimales para metro/kilo/litro si hace falta)
- Identificadores:
  - UUID o BIGINT (decisión por ADR; se recomienda UUID para SaaS-ready si no afecta POS)

---

## 8) Dominio: módulos principales (mapa)
> El detalle de entidades/campos va en `docs/06-DOMINIO-*.md` (tanda posterior).

- Tenant & Sucursales
- Usuarios/Roles/Permisos
- Catálogo: productos, variantes, UoM, empaques, kits
- Precios: listas, campañas, reglas de margen
- Inventario: stock, ubicaciones, movimientos, conteos, transferencias
- Compras: compras directas, recepciones, costos, prorrateos
- Ventas/POS: proformas, ventas, pagos, caja, impresión
- Clientes/CxC: abonos, estados de cuenta
- Proveedores/CxP: pagos, vencimientos
- Postventa: devoluciones, garantías
- Reportes/exports
- E-commerce: catálogo público, pedidos, reserva stock
- Fiscal (opcional): SRI, secuencias, documentos

---

## 9) Frontend: una app, dos “caras”
**Decisión práctica:** una sola app React con:
- Área privada: Admin/POS/backoffice
- Área pública: e-commerce (catálogo, carrito, pedido)

**Prioridades UX:**
- POS: teclado-friendly, búsqueda rápida, pocos pasos.
- Admin: formularios claros, validaciones, permisos visibles.
- E-commerce: catálogo rápido, filtros, checkout sencillo.

---

## 10) Observabilidad y auditoría
- Auditoría funcional: cambios y acciones sensibles.
- Logs técnicos: errores y eventos relevantes.
- Exportación de auditoría/reportes bajo permisos.

---

## 11) Reglas de estabilidad (anti deriva)
- Documentación BLOQUEADA: cambios solo RFC/ADR/CHANGELOG.
- Sprints BLOQUEADOS: no se editan.
- Estado DONE/APROBADO solo tras outputs del usuario y validación del asistente.
- Nombres Linux-strict y consistentes.

<!-- EOF -->