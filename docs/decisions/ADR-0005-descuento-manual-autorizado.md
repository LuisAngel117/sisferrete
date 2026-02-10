# ADR-0005 — Descuento manual con autorización
**Estado:** ACEPTADO (no editar; nuevos ADR para cambios)  
**Fecha:** 2026-02-09  

---

## Contexto
En mostrador se negocia, pero se debe evitar abuso.

## Decisión
- Se permite descuento manual solo con:
  - permiso explícito, o
  - supervisor override (clave) con auditoría.

## Consecuencias
- Control sin bloquear operación.
- Evidencia y trazabilidad en auditoría.

<!-- EOF -->
