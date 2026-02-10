# 00 — ÍNDICE / FUENTE DE VERDAD (sisferrete)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Propósito:** Este archivo es la “puerta de entrada” y el contrato de lectura. Si algo no está aquí, no se asume.  

---

## 1) Cómo usar esta documentación (regla de trabajo)
**Regla central:** el contexto del proyecto vive en `docs/**`. La conversación NO es fuente de verdad.

### 1.1 Orden de lectura obligatorio (para Codex y humanos)
Antes de ejecutar cualquier sprint, se debe leer en este orden:

1) `docs/00-INDICE.md` (este archivo)
2) `docs/01-BRIEF.md`
3) `docs/02-BRD.md`
4) `docs/03-ARQUITECTURA.md`
5) (TBD en tandas siguientes) `docs/04-CONVENCIONES.md`
6) (TBD) `docs/05-SEGURIDAD.md`
7) (TBD) `docs/06-DOMINIO-*.md`
8) (TBD) `docs/07-UX-UI-*.md`
9) (TBD) `docs/08-RUNBOOK.md`
10) (TBD) `docs/09-STAGE-RELEASE.md`
11) (TBD) `docs/CHANGELOG.md`
12) (TBD) `docs/sprints/SPR-MASTER.md`
13) Sprint a ejecutar: `docs/sprints/SPR-XXX.md`
14) Evidencia y trazabilidad:
    - (TBD) `docs/status/STATUS.md`
    - (TBD) `docs/log/LOG.md`

Si existe algún RFC/ADR relacionado, se lee ANTES de codificar:
- (TBD) `docs/rfcs/RFC-*.md`
- (TBD) `docs/decisions/ADR-*.md`

### 1.2 Regla anti-desviación
- Los sprints y docs **BLOQUEADOS** NO se editan directamente.
- Cambios SOLO vía:
  - RFC (propuesta de cambio)
  - ADR (decisión arquitectónica)
  - CHANGELOG (registro)

---

## 2) Macro-decisión: “qué es” sisferrete
**sisferrete** es un sistema vendible para ferreterías en Ecuador, diseñado para:
- Operación local-first (pruebas locales primero).
- Multi-sucursal.
- SaaS-ready desde arquitectura (tenant_id desde el inicio).
- POS rápido como prioridad.
- E-commerce desde el inicio (público), con checkout de pedido sin pasarela al inicio.
- Factura electrónica SRI como módulo opcional, diferido a fase “online”.

---

## 3) Decisiones cerradas (para evitar suposiciones)
Estas decisiones NO se re-discuten sin RFC/ADR:

### 3.1 Tenancy / Multi-sucursal
- Arquitectura SaaS-ready: `tenant_id` existe desde el inicio.
- Organización: Tenant (empresa/ferretería) → Sucursales → Usuarios.
- Una instalación puede operar con 1 tenant “demo”, pero el diseño soporta múltiples tenants.

### 3.2 Canales de venta
- Mostrador/POS (prioridad #1)
- Teléfono/WhatsApp (compartir proformas/estado, no “chatbot” completo)
- E-commerce: sí, desde el inicio:
  - Catálogo público + carrito + pedido
  - Pago: contra entrega / transferencia (sin pasarela al inicio)
  - Sin login al inicio (público) + datos al final

### 3.3 Unidades y cantidades
- Decimales permitidos **por tipo de unidad**:
  - UNIDAD / CAJA / PAQUETE: enteros
  - METRO / KILO / LITRO: decimales
- Redondeo de dinero: centavos (2 decimales).

### 3.4 Costeo
- Método global: Promedio ponderado (Weighted Average).

### 3.5 Descuentos
- Se permiten descuentos manuales **con autorización** (permiso o clave supervisor).
- También existen campañas.

### 3.6 Idioma
- UI: Español
- Código (clases/variables): Inglés (alineado a mercado laboral) con buenas prácticas.

### 3.7 Auth
- Login: email + contraseña
- 2FA TOTP: opcional y configurable (Admin/Superadmin)

### 3.8 Calidad
- Linters/formatters obligatorios desde el inicio (backend + frontend) cuando existan.

---

## 4) Mapa de documentación (tandas)
### 4.1 Base (TANDA 1 — esta entrega)
- `docs/00-INDICE.md` — contrato de lectura + decisiones cerradas
- `docs/01-BRIEF.md` — visión, alcance, restricciones
- `docs/02-BRD.md` — requisitos por módulo (funcional + no funcional)
- `docs/03-ARQUITECTURA.md` — arquitectura, multi-tenant, seguridad macro, datos, integraciones

### 4.2 Siguientes docs (TBD en tandas posteriores)
- Convenciones (naming Linux-strict, estilo, commits, i18n, etc.)
- Seguridad (permisos granulares, auditoría, hardening)
- Dominio (entidades/campos y reglas)
- UX/UI (pantallas, flujos POS, e-commerce, roles)
- Runbook / Stage-release / Changelog
- RFC/ADR templates
- SPR-MASTER + sprints

---

## 5) Glosario mínimo (términos del proyecto)
- **Tenant:** la “empresa” (ferretería) en modo SaaS.
- **Sucursal (Branch):** ubicación física; puede tener caja(s) y bodega/ubicación.
- **POS:** punto de venta/caja (flujo escaneo/búsqueda → cantidad → descuentos → pago → impresión).
- **SKU:** identificador interno de producto.
- **Código de barras:** identificador externo para búsqueda/escaneo.
- **UoM (Unit of Measure):** unidad de medida (unidad/metro/kilo/litro, etc.).
- **Stock reservado:** stock comprometido por pedidos (especialmente e-commerce).
- **READY_FOR_VALIDATION:** estado donde el sprint quedó implementado pero **aún no aprobado**.
- **DONE/APROBADO:** estado que se marca SOLO después de que el usuario ejecuta comandos y pega outputs; el asistente valida evidencias y autoriza el cambio.

---

## 6) Contrato de ejecución de sprints (plantilla de prompt)
Este es el patrón de prompt que se usará para ejecutar sprints, y se entregará “por sprint” cuando lleguemos a esa fase:

- Pre-check: git status limpio, git config user.* presente, rama.
- Lectura obligatoria del set de docs en orden.
- Scope estricto del sprint.
- Si hay huecos: RFC/ADR/CHANGELOG; NO editar sprint.
- Final: dejar sprint en READY_FOR_VALIDATION (no DONE).
- Usuario ejecuta comandos y pega outputs.
- Asistente decide si pasa a DONE/APROBADO y dicta cambios en STATUS/LOG.

---

## 7) Guardrail de archivos (EOF)
Todos los `.md` bajo `docs/**` deben terminar con:

`<!-- EOF -->`

No se aceptan docs truncados. Este guardrail es obligatorio.

<!-- EOF -->
