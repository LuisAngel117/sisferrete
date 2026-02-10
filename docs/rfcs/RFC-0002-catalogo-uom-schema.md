# RFC-0002 — Catálogo: nombre tabla UoM y campo decimal_scale
**Estado:** PROPUESTO  
**Regla:** un RFC propone; no reescribe historia.  

---

## 1) Resumen
- Título corto: Unificar contrato de UoM (tabla + campos)
- Autor: Codex
- Fecha: 2026-02-10
- Contexto (qué sprint/documento lo detectó): SPR-008 / docs/06-DOMINIO-PARTE-A.md

---

## 2) Problema / hueco detectado
- En `docs/06-DOMINIO-PARTE-A.md` la tabla UoM se nombra `uoms` e incluye `decimal_scale`.
- En `docs/sprints/SPR-008.md` la tabla se define como `units_of_measure` y **no** menciona `decimal_scale`.
- Esto genera ambigüedad de contrato para migraciones y APIs.

---

## 3) Objetivos
- Definir un nombre canónico de tabla para UoM.
- Alinear los campos mínimos obligatorios del catálogo base.

## 4) No objetivos
- No rediseñar el modelo de productos ni empaques.
- No cambiar el alcance del sprint en curso.

---

## 5) Propuesta
- Estándar de tabla: `units_of_measure` (según SPR-008).
- `decimal_scale` queda **pendiente** para decisión en un sprint posterior si se requiere precisión distinta por unidad.
- Actualizar `docs/06-DOMINIO-PARTE-A.md` para reflejar el nombre y campos aprobados.

---

## 6) Impacto
- Dominio: ajustar documentación de UoM.
- Arquitectura: bajo.
- Seguridad: n/a.
- UX/UI: n/a.
- Migraciones DB: solo si se decide agregar `decimal_scale`.
- Compatibilidad: requiere coherencia docs ↔ DB.

---

## 7) Alternativas consideradas
- Mantener `uoms` y renombrar sprint (no permitido sin RFC/ADR).
- Crear view/alias para compatibilidad (más complejidad sin necesidad actual).

---

## 8) Plan de implementación
- Paso 1: aprobar este RFC.
- Paso 2: actualizar `docs/06-DOMINIO-PARTE-A.md`.
- Paso 3: si se requiere `decimal_scale`, crear ADR + migración posterior.

---

## 9) Riesgos y mitigaciones
- Riesgo: inconsistencias futuras entre docs y DB.
  - Mitigación: cierre de RFC y actualización de docs.

---

## 10) Aprobación y cierre
- Decisión: Pendiente
- Si aprobado, ¿requiere ADR? No (salvo que se agregue `decimal_scale`)
- Links a ADR/Changelog/LOG/STATUS: TBD

<!-- EOF -->
