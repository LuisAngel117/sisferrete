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
| SPR-003 | DB + migraciones base (tenants/branches/users/roles/permissions) + seed demo | 1 | READY_FOR_VALIDATION | LOG 2026-02-10 | N/A | Commit: SPR-003: db migrations base + seed; Migraciones V1__platform_base.sql, V2__platform_seed.sql |
| TBD | TBD | TBD | TBD | TBD | TBD | Se completa cuando exista SPR-MASTER |

<!-- EOF -->
