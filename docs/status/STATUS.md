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
| TBD | TBD | TBD | TBD | TBD | TBD | Se completa cuando exista SPR-MASTER |

<!-- EOF -->
