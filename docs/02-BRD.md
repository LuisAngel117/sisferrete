# 02 — BRD (Business Requirements Document)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Contexto:** Ecuador, ferretería vendible, multi-sucursal, SaaS-ready (tenant_id).  

---

## 1) Principios del BRD
- “Sí o sí” se implementa lo que está aquí, respetando scope de sprints.
- Toda ambigüedad se resuelve con RFC/ADR antes de codificar.
- POS primero: decisiones se priorizan por velocidad y control operativo.

---

## 2) Requisitos transversales (aplican a todo)
### 2.1 Multi-tenant y multi-sucursal
- Todo dato “de negocio” pertenece a un `tenant_id`.
- La mayoría de operaciones están además acotadas a una `sucursal_id` (branch).
- Un usuario pertenece a un tenant y puede tener acceso a 1 o varias sucursales.
- Toda API debe validar contexto (tenant/sucursal) y permisos.

### 2.2 Permisos granulares
- Roles = conjuntos de permisos.
- Permisos = acciones concretas.
- Acciones sensibles requieren permiso explícito y auditoría.

### 2.3 Auditoría (obligatoria)
- Registrar: actor, acción, entidad afectada, antes/después (cuando aplica), fecha/hora, sucursal.
- Debe ser consultable por roles autorizados.

### 2.4 Unidades y cantidades
- Soporte a enteros y decimales por unidad:
  - Enteros: unidad/caja/paquete
  - Decimales: metro/kilo/litro
- Validación estricta en POS e inventario.

### 2.5 Redondeo y dinero
- Moneda USD.
- Montos se manejan con 2 decimales (centavos).
- Evitar errores de flotante (usar decimales exactos en backend).

---

## 3) Módulos y requisitos funcionales

### 3.1 Identidad, usuarios, roles y permisos
**Objetivo:** controlar acceso y evitar errores operativos.

**Requisitos:**
- Login por email+contraseña.
- 2FA TOTP opcional para Admin/Superadmin (configurable).
- Gestión de usuarios: alta/baja, asignación de roles, asignación de sucursales.
- Roles base recomendados:
  - SUPERADMIN, ADMIN, CAJERO, VENDEDOR, BODEGUERO, COMPRAS, CONTADOR, ECOMMERCE_MANAGER, AUDITOR
- Permisos sensibles mínimos (separados):
  - editar_iva_global
  - editar_secuencias_documentos
  - cambiar_precios
  - autorizar_precio_bajo_costo
  - editar_costos
  - crear_ajuste_stock
  - aprobar_ajuste_stock
  - abrir_caja / cerrar_caja
  - anular_venta / devolucion_venta / reimprimir_comprobante
  - gestionar_usuarios_roles
  - ver_auditoria

**AC (alto nivel):**
- Un CAJERO no puede editar costos/precios sin permiso.
- Acciones sensibles generan auditoría.

---

### 3.2 Catálogo de productos
**Objetivo:** encontrar y vender rápido, con estructura flexible.

**Requisitos:**
- Producto con:
  - SKU (interno) opcional
  - Código de barras opcional
  - Nombre, descripción corta
  - Categoría, marca
  - Unidad de medida base (unidad/metro/kilo/litro)
  - Flags: maneja_series, maneja_lotes, permite_decimal
- Variantes/atributos: medida, color, calibre, voltaje, etc.
- Empaques: unidad vs caja/paquete con conversión.
- Kits/combos: producto compuesto que descuenta componentes.
- Búsqueda rápida:
  - por nombre (parcial)
  - por SKU
  - por código de barras
  - filtros por categoría/marca/atributos
- Estado del producto: activo/inactivo (no borrar histórico).
- Soporte de imágenes (para UI y e-commerce) (storage TBD en fases).

**AC:**
- Se puede vender un producto por escaneo o por búsqueda.
- Un kit descuenta componentes correctamente.

---

### 3.3 Precios, listas, campañas y márgenes
**Objetivo:** controlar precios sin perder margen.

**Requisitos:**
- Múltiples listas de precios (ej: retail/mayorista).
- Campañas: descuentos por rango de fecha y reglas.
- Descuento manual: permitido solo con autorización (permiso o clave supervisor).
- No vender bajo costo/margen:
  - regla configurable por tenant
  - excepción con permiso `autorizar_precio_bajo_costo`
- Historial de precios (quién cambió, cuándo).

**AC:**
- Sin permiso, un cajero no puede aplicar descuento manual fuera de política.
- El sistema bloquea venta bajo costo salvo autorización.

---

### 3.4 Inventario y ubicaciones
**Objetivo:** stock confiable, sin negativos, con trazabilidad.

**Requisitos:**
- Por sucursal: una bodega con ubicación pasillo/estante (mínimo).
- Stock por producto/variante/unidad/empaque.
- No permitir stock negativo (bloqueo).
- Mínimos/máximos y alertas.
- Conteos cíclicos:
  - plan de conteo
  - registro de conteo
  - diferencias registradas para reportes
- Ajustes de inventario:
  - motivo obligatorio
  - aprobación obligatoria según permiso
- Transferencias entre sucursales:
  - salida (en tránsito)
  - recepción parcial
  - cierre de transferencia

**AC:**
- El sistema nunca deja stock negativo.
- Transferencia soporta recepción parcial.

---

### 3.5 Compras y costos
**Objetivo:** entrada de stock y costo real para margen confiable.

**Requisitos:**
- Compras directas (sin orden obligatoria).
- Recepción parcial de compra.
- Costo: promedio ponderado global.
- Gastos asociados (flete, etc.) prorrateados al costo.
- Devolución a proveedor (nota de crédito proveedor) (flujo detallado en dominio/UX).

**AC:**
- Compra parcial actualiza stock según recibido.
- Costo promedio se recalcula correctamente.

---

### 3.6 POS / Ventas / Caja
**Objetivo:** vender rápido, cobrar, imprimir, con control.

**Requisitos:**
- Venta por:
  - escaneo barcode
  - búsqueda rápida
- Soporte de cantidades enteras/decimales según unidad.
- Proforma/cotización:
  - crear proforma
  - convertir a venta sin reingreso
- Pagos: depende del cliente (se modela flexible):
  - efectivo, tarjeta, transferencia, mixto
- Caja:
  - apertura/cierre (configurable por cliente)
  - arqueos y registro de diferencias (si aplica)
- Impresión:
  - ticket en impresora cajero
  - documentos A4 para fiscal/online (más adelante)
- Devoluciones/anulaciones con permisos y auditoría.

**AC:**
- POS completa venta con flujo corto.
- Proforma se convierte sin reingresar.
- Operaciones sensibles requieren permisos.

---

### 3.7 Clientes y cuentas por cobrar
**Requisitos:**
- Cliente con datos configurables (según cliente real): cédula/RUC, etc.
- Abonos parciales.
- Estado de cuenta.
- Crédito configurable (si cliente lo requiere) — por ahora habilitable.

---

### 3.8 Proveedores y cuentas por pagar
**Requisitos:**
- Facturas proveedor y vencimientos.
- Pagos parciales y conciliación.
- Condiciones por proveedor (plazos, descuentos, retenciones si aplica).

---

### 3.9 Devoluciones, garantías y postventa
**Requisitos:**
- Devolución dentro de política (configurable).
- Garantía con seguimiento (por serie/fecha/proveedor/estado).
- Productos no retornables (reglas configurables).

---

### 3.10 Reportes y exportación
**Requisitos:**
- Ventas por día/caja/vendedor/sucursal.
- Utilidad/margen y rotación.
- Stock bajo y top productos.
- Compras por proveedor.
- Reportes fiscales (IVA, etc.).
- Exportación: PDF/Excel.
- Frecuencia: diario/semanal/mensual/anual (vista y export).

---

### 3.11 E-commerce (desde el inicio)
**Requisitos:**
- Catálogo público.
- Carrito.
- Checkout como pedido sin pasarela:
  - datos del cliente al final
  - opción “contra entrega” / “transferencia”
- Gestión de pedidos (backoffice):
  - estados: nuevo, confirmado, preparado, entregado, cancelado
- Stock reservado por pedido:
  - reserva al crear pedido
  - libera si se cancela
- No requiere login al inicio, pero debe dejar camino a “cuenta” futura.

---

### 3.12 Fiscal SRI (opcional, fase online)
**Requisitos (diferido):**
- Documentos y numeraciones.
- Factura electrónica (SRI) como módulo opcional.
- IVA editable pero protegido.
- Pruebas end-to-end cuando el sistema esté online.

---

## 4) Requisitos no funcionales
- **Rendimiento:** POS debe responder rápido (búsqueda/scan).
- **Seguridad:** permisos granulares + auditoría.
- **Usabilidad:** UI en español, mensajes claros.
- **Observabilidad:** logs útiles; auditoría consultable.
- **Portabilidad:** desarrollo local-first; docker/deploy en fase posterior.
- **Calidad:** linters/formatters, estándares consistentes.

<!-- EOF -->