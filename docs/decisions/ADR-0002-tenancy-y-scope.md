# ADR-0002 — Tenancy SaaS-ready + scoping por sucursal
**Estado:** ACEPTADO (no editar; nuevos ADR para cambios)  
**Fecha:** 2026-02-09  

---

## Contexto
El sistema debe ser vendible a clientes con una o múltiples sucursales y debe estar listo para SaaS en el futuro.

## Decisión
- `tenant_id` existe desde el inicio en todas las entidades de negocio.
- Operaciones de sucursal incluyen `branch_id`.
- El contexto de tenant/sucursal se valida en backend; la UI solo ayuda.

## Consecuencias
- Evita fugas de datos entre tenants.
- Facilita multi-sucursal real sin hacks.

<!-- EOF -->
