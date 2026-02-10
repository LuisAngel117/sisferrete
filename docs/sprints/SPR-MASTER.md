# SPR-MASTER — Índice maestro de sprints (sisferrete)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Propósito:** Este archivo fija el **plan estable** de sprints (nombres, orden, alcance macro, gates de validación).  
**Regla anti-deriva:** los títulos/orden NO cambian a mitad del proyecto sin RFC/ADR/CHANGELOG.

---

## 1) Reglas de gobernanza para TODOS los sprints
1) Cada `SPR-XXX.md` estará **BLOQUEADO** y NO se editará. Cambios solo por RFC/ADR/CHANGELOG.  
2) Codex ejecuta el sprint y deja el estado en **READY_FOR_VALIDATION**.  
3) El usuario ejecuta comandos y pega outputs.  
4) El asistente valida evidencias y dicta el cambio a **DONE/APROBADO** en `docs/status/STATUS.md` + entrada en `docs/log/LOG.md`.  
5) Si un sprint NO entrega algo aún “probable” (porque el módulo se completa en varios sprints), el sprint debe declarar **N/A o validación parcial**, y se mantiene READY_FOR_VALIDATION con nota “validación diferida a Gate”.

---

## 2) Stages del producto
- **Stage 0:** Docs/Gobernanza (anti-desviación, plantillas, verificadores).
- **Stage 1:** Core local (catálogo, inventario, compras, POS, permisos, auditoría, reportes) — validable local.
- **Stage 2:** E-commerce (catálogo público, carrito, pedidos, reservas, backoffice) — validable local.
- **Stage 3:** Fiscal opcional “online” (SRI, secuencias, documentos) — se prueba cuando haya entorno online.
- **Stage 4:** Deploy (Docker/CI/Release) — después de core validado.

---

## 3) Concepto clave: “Validation Gates” (para tu regla de módulos completos)
Para evitar validar funcionalidades “a medias”, se definen **Gates**:
- Un Gate es el **último sprint** de un bloque funcional (módulo).
- En Gate se exige validación funcional completa (con comandos/outputs del usuario).
- En sprints previos del módulo, la validación puede ser parcial o N/A, pero el sprint queda READY_FOR_VALIDATION y NO pasa a DONE/APROBADO hasta que el Gate esté validado.

**Convención:**
- `Gate = YES` → aquí se valida completo el módulo.
- `Gate = NO` → sprint intermedio (puede tener validación parcial/N/A).

---

## 4) Tabla maestra de sprints (plan estable)
> Nota: la duración objetivo por sprint es 45–90 min, pero algunos pueden requerir dividirse en sub-sprints si el nivel de detalle lo exige.  
> Si un sprint resulta demasiado grande, se crea RFC para dividirlo sin romper nombres/orden (o se añade `SPR-0XXA` por ADR si fuese estrictamente necesario).

| Sprint | Título (estable) | Stage | Módulo | Gate | Objetivo macro | Resultado esperado |
|------:|-------------------|:-----:|--------|:----:|----------------|-------------------|
| SPR-001 | Governance de ejecución + anti-desviación (Local-first) | 0 | Gobernanza | YES | Guardrails, verificación docs, trazabilidad STATUS/LOG | Repo listo para ejecutar sprints sin perder contexto |
| SPR-002 | Scaffolding monorepo (backend+frontend) + tooling base | 1 | Plataforma | NO | Crear estructura backend/frontend, scripts base, lint/format (mínimo) | Proyecto arranca (placeholder) sin features |
| SPR-003 | DB + Migraciones base (tenants/branches/users/roles/permissions) + seed demo | 1 | Plataforma | NO | Esquema inicial + seeds (tenant “Ferreteria”) | DB reproducible localmente |
| SPR-004 | Auth base (email+password, JWT/refresh) + scoping tenant/branch | 1 | Plataforma | YES | Login, contexto tenant/branch, guards iniciales | Gate: login + scoping funcionando local |
| SPR-005 | Auditoría base (audit_events) + hook para acciones sensibles | 1 | Seguridad/Auditoría | NO | Infra auditoría + registro mínimo | Acciones clave registrables |
| SPR-006 | Configuración crítica (IVA, políticas) + permisos protegidos | 1 | Seguridad/Auditoría | NO | Modelo tenant_config + permisos (config.iva.edit) | IVA editable pero ultra protegido |
| SPR-007 | Gestión de usuarios/roles/permisos + acceso a sucursales | 1 | Seguridad/Auditoría | YES | Admin UI/API para usuarios, roles, permisos y scopes | Gate: IAM completo usable |
| SPR-008 | Catálogo base: categorías, marcas, UoM (decimales por unidad) | 1 | Catálogo | NO | CRUD base + validaciones UoM | UoM gobierna enteros/decimales |
| SPR-009 | Productos base (SKU/barcode), búsqueda rápida, estado activo | 1 | Catálogo | NO | CRUD producto + search (POS-ready) | Búsqueda por nombre/SKU/barcode |
| SPR-010 | Variantes/atributos + barcode por variante | 1 | Catálogo | NO | Variantes con atributos_json + UX de selección | Variantes vendibles |
| SPR-011 | Empaques/conversiones (unidad vs caja/paquete) | 1 | Catálogo | NO | Packaging + units_per_pack | Venta por empaque consistente |
| SPR-012 | Precios: listas + entries + campañas + historial | 1 | Catálogo/Precios | NO | Motor de precios con listas y campañas | Precio correcto por lista/campaña |
| SPR-013 | Reglas comerciales: descuento manual autorizado + venta bajo costo | 1 | Catálogo/Precios | YES | Supervisor override / permisos + auditoría | Gate: precios/desc controlados |
| SPR-014 | Inventario base: stock_balances, locations, movimientos (kardex) | 1 | Inventario | NO | Estructura de stock y movimientos | Kardex base listo |
| SPR-015 | Compras directas + recepción parcial + prorrateo + costeo promedio | 1 | Compras/Costos | NO | Flujo de compra y actualización de costo | Costeo estable y reproducible |
| SPR-016 | Ajustes de inventario (create/approve/apply) + auditoría | 1 | Inventario | NO | Workflow de ajustes con permisos | Ajustes controlados |
| SPR-017 | Transferencias entre sucursales + recepción parcial | 1 | Inventario | NO | Transfer con tránsito/recepción parcial | Transferencias reales |
| SPR-018 | Conteos cíclicos + registro de diferencias + reportes de diferencias | 1 | Inventario | YES | Conteo → diferencias → (opcional) ajuste | Gate: inventario operativo completo |
| SPR-019 | POS base: carrito de venta, scan/búsqueda, cantidades por UoM, totales | 1 | POS/Ventas | NO | Flujo rápido de venta (sin caja avanzada) | Venta armable sin fricción |
| SPR-020 | Pagos + caja (apertura/cierre configurable) + ticket placeholder | 1 | POS/Ventas | NO | Pagos multi-método + cash_sessions | Cobro y control caja base |
| SPR-021 | Proformas/cotizaciones + convertir a venta sin reingreso | 1 | POS/Ventas | NO | Quotes + conversión | Proforma→venta funcional |
| SPR-022 | Anulación/devolución + reverso stock + permisos/auditoría | 1 | POS/Ventas | NO | Flujos sensibles controlados | Reversos consistentes |
| SPR-023 | Clientes + CxC (abonos/estado cuenta) + mínimos de cuentas | 1 | POS/Ventas | YES | Cobranza básica y control cliente | Gate: POS completo usable local |
| SPR-024 | Reportes operativos base + exportación (PDF/Excel/CSV) | 1 | Reportes | NO | Ventas, margen, stock bajo, top productos, compras | Reportes exportables |
| SPR-025 | Auditoría viewer + reportes sensibles protegidos por permisos | 1 | Reportes | YES | UI/API auditoría + seguridad reports | Gate: reporting seguro |
| SPR-026 | E-commerce público: catálogo, filtros, detalle producto | 2 | E-commerce | NO | Navegación pública rápida | Catálogo web usable |
| SPR-027 | Carrito + checkout (pedido sin pasarela) + reserva stock | 2 | E-commerce | NO | Crear web_order y reservar stock | Pedidos generan reservas |
| SPR-028 | Backoffice pedidos: estados + cancelar/liberar reserva + asignar sucursal | 2 | E-commerce | NO | Gestión operativa de pedidos | Pedidos gestionables |
| SPR-029 | Convertir pedido web a venta POS + consistencia stock | 2 | E-commerce | NO | Integración ecom→venta | Flujo completo consistente |
| SPR-030 | Pulido e-commerce + UX + performance + hardening básico | 2 | E-commerce | YES | Mejoras finales y validación completa | Gate: e-commerce listo |
| SPR-031 | Fiscal opcional: secuencias/documentos (modelo) + protecciones | 3 | Fiscal | NO | Document sequences + permisos/auditoría | Base fiscal preparada |
| SPR-032 | SRI e-invoicing (módulo opcional) + estrategia de pruebas online | 3 | Fiscal | YES | Integración SRI (cuando online) + toggles | Gate: fiscal listo para online |
| SPR-033 | Docker (compose) + configuración de despliegue | 4 | Deploy | NO | Contenerización para backend/frontend/db | Deploy reproducible |
| SPR-034 | CI básico + runbook release + hardening final | 4 | Deploy | YES | Pipeline mínimo y guía de release | Gate: producto listo para entrega |

---

## 5) Regla de estabilidad de nombres y alcance
- El sprint mantiene su título y propósito.
- Si se descubre un hueco:
  - NO se edita el sprint.
  - Se crea RFC y (si aplica) ADR.
  - El cambio se refleja en CHANGELOG.
- Si un sprint necesita dividirse por tamaño:
  - Se propone por RFC y se aprueba por ADR si impacta el plan maestro.

---

## 6) Próximo paso
Una vez aprobado este índice maestro:
1) Se crean los archivos `docs/sprints/SPR-001.md ... SPR-034.md` en tandas, usando el **modelo canónico** (como el SPR-001 que ya compartiste).
2) Cada sprint incluirá:
   - Objetivo
   - Scope (Incluye/Excluye)
   - Entregables
   - AC verificables
   - Smoke test manual (comandos que tú ejecutarás)
   - “Comandos verdad”
   - DoD + regla READY_FOR_VALIDATION

<!-- EOF -->
