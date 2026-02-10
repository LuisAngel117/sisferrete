# STATUS — Trazabilidad de sprints
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Regla:** este archivo es la tabla de control. No se reescribe historia.

---

## 1) Estados permitidos
- **PLANNED:** existe en SPR-MASTER pero no iniciado.
- **IN_PROGRESS:** Codex está trabajando (raro, preferir cambios por sprint).
- **READY_FOR_VALIDATION:** implementado/actualizado, pendiente validación humana.
- **DONE/APROBADO:** validado por outputs del usuario y aprobado por el asistente.
- **BLOCKED:** bloqueado por decisión abierta o dependencia; requiere RFC/ADR.

> Codex NO marca DONE/APROBADO.  
> DONE/APROBADO se marca solo cuando el usuario ejecuta comandos y pega outputs, y el asistente valida.

---

## 2) Tabla de sprints
> Se llena cuando exista `docs/sprints/SPR-MASTER.md`.  
> No inventar sprints aquí antes del índice maestro.

| Sprint | Título | Stage | Estado | Evidencia (LOG) | RFC/ADR Relacionados | Notas |
|------:|--------|:-----:|--------|-----------------|----------------------|------|
| SPR-002 | Scaffolding monorepo (backend+frontend) + tooling base | 1 | DONE/APROBADO | LOG 2026-02-10 | N/A | Evidencia validada (mvnw test/run, health/ping, npm install/dev) |
| SPR-003 | DB + migraciones base (tenants/branches/users/roles/permissions) + seed demo | 1 | DONE/APROBADO | LOG 2026-02-10 | N/A | Evidencia validada: Flyway V1/V2 OK (Postgres 17.7) |
| SPR-005 | Auditoría base (audit_events) + hook para acciones sensibles | 1 | READY_FOR_VALIDATION | LOG 2026-02-09 | N/A | commit: 26195b3; archivos: V4__audit_events_columns.sql, AuditService, AuthService, SecurityConfig, sprint5.ps1 |
| SPR-006 | Configuración crítica (IVA) + permisos protegidos + auditoría | 1 | READY_FOR_VALIDATION | LOG 2026-02-09 | N/A | archivos: V5__tenant_config_vat_bps.sql, TenantConfig*, App.tsx |
| SPR-007 | Gestión de usuarios/roles/permisos + acceso a sucursales | 1 | READY_FOR_VALIDATION | LOG 2026-02-09 | N/A | archivos: V6__iam_seed_and_constraints.sql, Iam*, Admin*Controller, sprint7.ps1, App.tsx |
| SPR-008 | Catálogo base: categorías, marcas, UoM (decimales por unidad) | 1 | READY_FOR_VALIDATION | LOG 2026-02-10 | RFC-0002 | commit: TBD; archivos: V7__catalog_base.sql, Catalog*, AdminCatalogController, sprint8.ps1, App.tsx |
| SPR-009 | Productos base (SKU/barcode), búsqueda rápida, estado activo | 1 | READY_FOR_VALIDATION | LOG 2026-02-10 | N/A | archivos: V8__products_base.sql, Product*, AdminProductsController, sprint9.ps1, App.tsx |
| TBD | TBD | TBD | TBD | TBD | TBD | Se completa cuando exista SPR-MASTER |

<!-- EOF -->
