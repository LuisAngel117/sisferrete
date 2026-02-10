# 09 — STAGE / RELEASE (fases del producto)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  

---

## 1) Propósito
Definir fases (stages) y gates para:
- mantener enfoque local-first
- evitar meter Docker/online antes de tiempo
- ejecutar SRI como módulo opcional al final

---

## 2) Stages
### Stage 0 — Docs/Gobernanza
- Documentación cerrada, convenciones, seguridad, dominio, UX.
- Guardrails anti-desviación listos.

**Gate:** docs consistentes + templates RFC/ADR + STATUS/LOG.

### Stage 1 — Core local (operación ferretería)
- Catálogo, inventario, compras, POS, roles/permisos, auditoría, reportes base.

**Gate:** módulos core funcionales localmente y validados por outputs.

### Stage 2 — E-commerce (local)
- Catálogo público, carrito, pedidos, reserva stock, backoffice pedidos.

**Gate:** pedidos y reservas consistentes, sin romper stock.

### Stage 3 — Fiscal opcional “online”
- SRI (factura electrónica) opcional.
- Secuencias/documentos.
- Pruebas end-to-end en entorno online.

**Gate:** módulo fiscal probado y aislado del core.

### Stage 4 — Deploy
- Docker, despliegue, observabilidad, hardening.
- Pruebas online completas.

---

## 3) Política de releases
- Releases se hacen cuando un conjunto de sprints está DONE/APROBADO.
- Cambios de alcance => RFC/ADR.
- CHANGELOG siempre actualizado.

<!-- EOF -->
