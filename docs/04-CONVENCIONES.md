# 04 — CONVENCIONES (naming, estilo, control y trazabilidad)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Objetivo:** eliminar ambigüedad operativa y técnica para evitar desviaciones y retrabajo.  

---

## 1) Principios
1) **Local-first:** todo debe poder ejecutarse y validarse localmente antes de pensar en Docker/online.  
2) **Sprints y docs bloqueados:** no se editan; cambios por RFC/ADR/CHANGELOG.  
3) **Permisos granulares siempre:** roles son “paquetes”, permisos son el control real.  
4) **Auditoría obligatoria** para acciones sensibles.  
5) **Sin inventario negativo** (regla de oro).  
6) **POS primero:** decisiones de UX y rendimiento priorizan caja.

---

## 2) Convenciones de archivos y nombres (Linux-strict)
### 2.1 Reglas de naming
- **Archivos y carpetas:** respetar EXACTAMENTE el casing acordado.
- Evitar espacios, tildes/ñ y caracteres raros en rutas.
- Preferencia para nuevos nombres: `lowercase-kebab-case` (siempre que no choque con el estándar ya existente en docs).

> Importante: En este repo ya existen archivos en MAYÚSCULAS (ej: `SPR-001.md`).  
> **No se crean variantes** como `spr-001.md`. En Linux serían archivos distintos.

### 2.2 Markdown guardrails
- Todo `.md` en `docs/**` debe terminar con el marcador exacto:
  - `<!-- EOF -->`
- No se aceptan docs truncados.
- Formato:
  - Encabezado con estado (BLOQUEADO o ACEPTADO).
  - Secciones numeradas (1, 2, 3…).
  - Listas con criterios verificables (AC) cuando aplique.

---

## 3) Reglas de Git (flujo y commits)
### 3.1 Rama y commits
- No se asume nombre de rama (puede ser `main`, `master`, `dev`); se reporta en pre-check.
- Commits:
  - **Atómicos**: un commit por intención.
  - Mensaje recomendado:
    - Docs: `DOCS(Tn): ...`
    - Sprints: `SPR-XXX: ...`

### 3.2 Prohibiciones
- No se hace “reformat masivo” sin RFC (rompe diffs).
- No se renombra archivos por capricho (rompe Linux y trazabilidad).

---

## 4) Gobernanza de cambios (RFC/ADR/CHANGELOG)
### 4.1 RFC
- RFC = propuesta o aclaración cuando hay huecos/contradicciones o cambios deseados.
- Un RFC **no cambia historia**, propone un cambio con impacto y plan.

### 4.2 ADR
- ADR = decisión técnica/arquitectónica.
- ADRs son **inmutables** una vez aceptados: si cambia algo, se crea un ADR nuevo.

### 4.3 CHANGELOG
- Se actualiza en cada cambio relevante (docs o features).
- No se reescriben entradas pasadas.

---

## 5) Convenciones de dominio (invariantes)
Estas reglas son “hard constraints” del sistema:

- `tenant_id` obligatorio en entidades de negocio.
- `branch_id` obligatorio en operaciones de sucursal (ventas, stock, caja, etc.).
- Cantidades:
  - UNIDAD/CAJA/PAQUETE: enteros
  - METRO/KILO/LITRO: decimales
- Moneda USD, redondeo a centavos (2 decimales).
- Costeo: promedio ponderado global.
- No inventario negativo.
- Descuento manual: solo con autorización (permiso o clave de supervisor).

---

## 6) Convenciones técnicas (cuando exista código)
> Esta sección define el “contrato” para cuando se creen backend/frontend.

### 6.1 Idioma
- **Código (clases/variables):** Inglés (market-aligned).
- **UI:** Español.
- **Mensajes de error para usuario:** Español, claros y accionables.

### 6.2 Backend (Java/Spring)
- Capas: `api` / `application` / `domain` / `infrastructure`.
- Tipos:
  - Dinero: decimal exacto (evitar float/double).
  - Cantidades: decimal exacto con escala definida por UoM.
- Validaciones:
  - entrada en `api` (DTO validation)
  - invariantes en `domain`
  - permisos en `application`
- Excepciones:
  - mapear a HTTP con códigos coherentes (400/401/403/404/409/422/500).
- Logs:
  - no loggear secretos.
  - logs útiles y concisos.
- Tests:
  - unit tests para reglas de dominio.
  - tests de scoping (tenant/branch) para endpoints clave.

### 6.3 Frontend (React/TS)
- UI: Español.
- Accesibilidad básica (labels, focus).
- POS:
  - teclado-friendly
  - pocas pantallas/pasos
  - búsqueda instantánea
- Estado global:
  - evitar sobre-complejidad, preferir patrones simples.
- Manejo de permisos:
  - ocultar/inhabilitar acciones sin permiso
  - aun así, backend valida (no confiar en UI)

### 6.4 Formateo/Lint (obligatorio desde inicio del código)
- Backend: formatter + linter (ej: Spotless + Checkstyle o equivalente).
- Frontend: ESLint + Prettier.
- Config exacta se definirá cuando exista scaffold (ADR si es necesario).

---

## 7) Convención de estados de sprint (trazabilidad)
**Regla global:**
- Codex nunca marca **DONE/APROBADO**.
- Codex deja el sprint en **READY_FOR_VALIDATION**.
- El usuario ejecuta comandos y pega outputs.
- El asistente valida evidencias y dicta el cambio a **DONE/APROBADO** en `docs/status/STATUS.md`, registrando evidencia en `docs/log/LOG.md`.

---

## 8) Convención de “Comandos verdad”
- Cada sprint define sus comandos de validación.
- Si el sprint no produce una funcionalidad probables todavía:
  - el sprint debe declarar “N/A” con explicación, o pruebas parciales.
- El usuario ejecuta comandos; el asistente solo interpreta outputs.

<!-- EOF -->
