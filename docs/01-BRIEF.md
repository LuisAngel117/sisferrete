# 01 — BRIEF (Visión, alcance y restricciones)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Producto:** sisferrete (ferretería)  
**UI:** Español | **Código:** Inglés  

---

## 1) Resumen ejecutivo
sisferrete es un sistema de gestión integral para ferreterías en Ecuador con enfoque “producto vendible”.
Prioridades:
1) POS/caja rápido y confiable.
2) Inventario exacto (sin negativo) con control y auditoría.
3) Compras/costos consistentes (promedio ponderado).
4) E-commerce desde el inicio (público) como canal adicional.
5) Preparación para “online” (SRI/factura electrónica opcional) sin bloquear el core local-first.

---

## 2) Objetivo del producto
### 2.1 Qué problema resuelve
- Reducir errores de caja y de stock.
- Evitar pérdidas por ajustes sin control.
- Acelerar ventas con búsqueda rápida y escaneo.
- Controlar precios, márgenes y descuentos con permisos.
- Proveer reportes útiles (operativos y fiscales) con exportación.
- Permitir ventas por mostrador y soporte a pedidos (WhatsApp/e-commerce).

### 2.2 Qué significa “listo para vender”
- La primera versión debe ser usable por usuario final sin asistencia:
  - UI clara, consistente, con iconos/colores (cuando exista frontend).
  - Flujos POS rápidos, con validaciones y mensajes explícitos.
  - Roles/permisos que eviten “meter las patas”.
  - Auditoría de operaciones sensibles.
  - Reportes base exportables.

---

## 3) Alcance macro (módulos)
### 3.1 Incluye (sí o sí)
- Tenancy SaaS-ready (tenant_id desde el inicio).
- Multi-sucursal (sucursales operativas).
- Usuarios, roles y permisos granulares.
- Catálogo de productos (SKU/código barras), categorías, marcas, variantes.
- Unidades de medida + reglas de decimales por unidad.
- Empaques (unidad vs caja/paquete) con conversiones.
- Series/lotes (según producto) y garantías.
- Kits/combos (descuenta componentes).
- Inventario sin negativo + ubicaciones pasillo/estante.
- Conteos cíclicos con registro de diferencias.
- Transferencias entre sucursales con tránsito/recepción parcial.
- Ajustes con motivo + aprobación.
- Compras directas + recepción parcial.
- Costeo promedio ponderado + prorrateo de gastos.
- POS/caja: ventas, pagos, impresión ticket, proforma → venta.
- Descuentos manuales con autorización + campañas.
- Clientes y cuentas por cobrar (abonos parciales / estado de cuenta).
- Proveedores y cuentas por pagar (pagos parciales / conciliación).
- Devoluciones y garantías.
- Reportes (operativos + fiscales) + exportación (PDF/Excel).
- E-commerce: catálogo público + carrito + pedido (sin pasarela al inicio).
- Auditoría: quién/qué/cuándo/antes-después.

### 3.2 Excluye (por ahora)
- Pasarela de pago online (se podría agregar por RFC).
- Integración contable directa (solo exportación).
- Factura electrónica SRI: existe como módulo opcional, pero se implementa y prueba en fase online (sin bloquear core).

---

## 4) Usuarios tipo (personas)
- **Cajero (POS):** busca/escanea productos, cobra, imprime ticket, convierte proformas.
- **Vendedor (Mostrador sin caja):** arma proformas, asesora, crea pedidos, gestiona clientes.
- **Bodeguero:** recibe compras, ubica stock, hace transferencias y conteos.
- **Compras:** registra compras y costos, gestiona proveedores.
- **Admin:** configura operación (usuarios/roles, precios, autorizaciones, reportes).
- **Superadmin:** soporte/instalación/multi-tenant, configuración crítica.
- **Contador/Auditor:** consulta reportes/exports y auditoría.

---

## 5) Restricciones y no negociables
- Local-first: el core debe funcionar y probarse localmente.
- Sin inventario negativo.
- Permisos granulares obligatorios (no “solo roles”).
- Auditoría obligatoria para operaciones sensibles.
- Linux-strict: rutas y nombres consistentes (case-sensitive).
- Documentación y sprints bloqueados + cambios via RFC/ADR/CHANGELOG.
- Estado DONE/APROBADO solo se marca tras evidencias del usuario y validación del asistente.

---

## 6) Criterios de éxito (producto)
- POS fluye rápido: búsqueda eficiente por nombre/SKU/barcode, pocos clics.
- Nadie puede cambiar IVA/precios críticos/costos/stock sensible sin permiso.
- Reportes dan confianza (margen basado en costeo acordado).
- E-commerce genera pedidos y reserva stock sin romper inventario.
- El sistema soporta 1 o varias sucursales sin hacks.

---

## 7) Roadmap por “Stages” (alto nivel)
> Detalle por sprints se define en `SPR-MASTER.md` más adelante.

- **Stage 0 (Docs/Gobernanza):** cerrar docs, convenciones, anti-desviación.
- **Stage 1 (Core local):** catálogo, inventario, compras, POS base, roles/permisos.
- **Stage 2 (E-commerce):** catálogo público, carrito, pedido, stock reservado, backoffice pedidos.
- **Stage 3 (Fiscal/online opcional):** SRI factura electrónica, numeraciones/secuencias, documentos.
- **Stage 4 (Deploy):** Docker, despliegue, online end-to-end.

<!-- EOF -->
