# CHANGELOG — sisferrete
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
Formato basado en “Keep a Changelog”, adaptado a este proyecto.

---

## [Unreleased]
### Added
- Docs base (TANDA 1): INDICE, BRIEF, BRD, ARQUITECTURA.
- Docs base (TANDA 2): CONVENCIONES, SEGURIDAD, DOMINIO, UX/UI, RUNBOOK, STAGE-RELEASE, templates RFC/ADR, STATUS/LOG.
- Backend/Frontend scaffold base.
- DB platform base + Flyway.
- Audit base + hooks (SPR-005).
- Tenant config IVA protegido (SPR-006).
- IAM admin usable + branch access (SPR-007).
- Catálogo base (categorías, marcas, UoM) + permiso CATALOG_MANAGE (SPR-008).
- RFC-0002: contrato UoM (tabla + campo decimal_scale).
- Productos base + búsqueda rápida (SPR-009).

### Changed
- N/A

### Fixed
- npm install dependency conflict in frontend (ESLint/TypeScript ESLint).
- Flyway actualizado para soporte de PostgreSQL 17.x.

---

<!-- EOF -->
