# ADR-0006 — Gobernanza de ejecución: READY_FOR_VALIDATION → DONE/APROBADO
**Estado:** ACEPTADO (no editar; nuevos ADR para cambios)  
**Fecha:** 2026-02-09  

---

## Contexto
Se detectó deriva cuando se marca DONE sin validación humana real.

## Decisión
- Codex ejecuta sprints y deja el estado en **READY_FOR_VALIDATION**.
- El usuario ejecuta comandos y pega outputs.
- El asistente valida outputs y dicta el cambio a **DONE/APROBADO** en STATUS/LOG.

## Consecuencias
- Trazabilidad fuerte.
- Menos falsos positivos de “terminado”.

<!-- EOF -->
