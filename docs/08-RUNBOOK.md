# 08 — RUNBOOK (operación local-first)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Objetivo:** que cualquier persona pueda preparar el entorno local sin inventar pasos.  

---

## 1) Prerrequisitos (local)
> Versiones exactas se fijan cuando exista scaffold.

- Git instalado
- Java 21 (recomendado)
- Node.js (para frontend; versión a definir cuando exista)
- PostgreSQL local (16+; 17 recomendado)
- psql disponible en PATH (ideal)

---

## 2) Reglas de seguridad local
- No commitear secretos.
- Configurar variables en `.env` (cuando exista) pero no subirlo.

---

## 3) Base de datos local (contrato)
> Este runbook define el enfoque. Comandos finales se completan cuando exista backend.

- DB: PostgreSQL local instalable.
- Migraciones: Flyway (default por ADR; si se cambia, RFC/ADR).
- En local habrá seed mínimo:
  - tenant “Ferreteria”
  - sucursal “Matriz” (o equivalente)
  - usuarios demo (Admin/Superadmin) (definición posterior)

---

## 4) Comandos “verdad” (TBD hasta que exista código)
Cuando exista backend/frontend, cada sprint definirá:
- Backend:
  - `./mvnw test`
  - `./mvnw spring-boot:run`
- Frontend:
  - `npm run build`
  - `npm run dev`
  - (tests) si aplica

> Por ahora: no se inventan comandos inexistentes.

---

## 5) Verificadores de documentación
- Todos los docs deben terminar con `<!-- EOF -->`.
- Se proveerá un script de verificación en sprints iniciales (ej: `scripts/verify/verify-docs-eof.ps1`).

---

## 6) Troubleshooting (plantilla)
- Problema:
- Contexto:
- Comandos ejecutados:
- Output:
- Resolución:

<!-- EOF -->
