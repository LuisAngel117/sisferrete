# RFC-0003 — Catálogo: contrato de presentaciones/empaques
**Estado:** PROPUESTO  
**Regla:** un RFC propone; no reescribe historia.  

---

## 1) Resumen
- Título corto: Unificar contrato de presentaciones/empaques (tabla + campos)
- Autor: Codex
- Fecha: 2026-02-10
- Contexto (qué sprint/documento lo detectó): SPR-011 / docs/06-DOMINIO-PARTE-A.md

---

## 2) Problema / hueco detectado
- En `docs/06-DOMINIO-PARTE-A.md` se define `product_packaging` con `units_per_pack` y sin `sale_uom_id/base_uom_id`.
- En `docs/sprints/SPR-011.md` el contrato exige `product_packagings` con:
  - `sale_uom_id`
  - `base_uom_id`
  - `base_units_per_sale_unit`
  - `is_default_for_sale`
- Esto crea ambigüedad sobre el modelo real de empaques.

---

## 3) Objetivos
- Definir contrato canónico para empaques/presentaciones.
- Alinear docs de dominio con el sprint implementado.

## 4) No objetivos
- No redefinir reglas de pricing ni inventario por empaque (sprints posteriores).
- No cambiar el alcance del sprint en curso.

---

## 5) Propuesta
- Estándar de tabla: `product_packagings` (según SPR-011).
- Campos obligatorios:
  - `sale_uom_id`, `base_uom_id`, `base_units_per_sale_unit`
  - `is_default_for_sale`, `is_active`, `barcode` opcional
- Actualizar `docs/06-DOMINIO-PARTE-A.md` para reflejar el contrato aprobado.

---

## 6) Impacto
- Dominio: ajustar definición de empaques.
- Arquitectura: bajo.
- Seguridad: n/a.
- UX/UI: n/a.
- Migraciones DB: ya resuelto en SPR-011.
- Compatibilidad: alinea docs con DB.

---

## 7) Alternativas consideradas
- Mantener `product_packaging` y mapear a `product_packagings` (duplica modelos).
- Usar `units_per_pack` sin `sale_uom_id` (pierde contexto de UoM de venta).

---

## 8) Plan de implementación
- Paso 1: aprobar este RFC.
- Paso 2: actualizar `docs/06-DOMINIO-PARTE-A.md`.

---

## 9) Riesgos y mitigaciones
- Riesgo: futuros módulos (pricing/stock) asuman naming distinto.
  - Mitigación: cerrar RFC y actualizar docs antes de esos sprints.

---

## 10) Aprobación y cierre
- Decisión: Pendiente
- Si aprobado, ¿requiere ADR? No
- Links a ADR/Changelog/LOG/STATUS: TBD

<!-- EOF -->
