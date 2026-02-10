# LOG — Bitácora de ejecución
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Regla:** solo agregar entradas al final. No reescribir.
`
---
`
## Plantilla de entrada (copiar/pegar)
**YYYY-MM-DD HH:MM:SS -05:00 (America/Guayaquil)**  
Sprint: SPR-XXX  
Estado resultante: READY_FOR_VALIDATION / DONE/APROBADO / BLOCKED  
Resumen:
- Qué se hizo
- Qué se cambió (archivos clave)
`
Evidencia:
- Comandos ejecutados por el usuario:
  - `<comando>`
- Output:
```text
<output>
```
`
---
`
**2026-02-09 20:25:03 -05:00 (America/Guayaquil)**
Sprint: SPR-002
Estado resultante: READY_FOR_VALIDATION
Resumen:
- Scaffold backend/ frontend base (sin features)
- Script smoke y configuraciones mínimas
`
Evidencia:
- Comandos ejecutados por el usuario:
  - cd backend && ./mvnw test
  - cd backend && ./mvnw spring-boot:run
  - curl http://localhost:8080/actuator/health
  - curl http://localhost:8080/api/ping
  - cd frontend && npm install
  - cd frontend && npm run dev
- Output:
```text
PENDIENTE (usuario debe ejecutar y pegar outputs)
```
---
`
**2026-02-09 20:40:00 -05:00 (America/Guayaquil)**
Sprint: SPR-002
Estado resultante: READY_FOR_VALIDATION
Resumen:
- Ajuste de versiones ESLint para permitir npm install
- Sin cambios de alcance funcional
`
Evidencia:
- Comandos ejecutados por el usuario:
  - cd frontend && npm install
- Output:
`	ext
PENDIENTE (usuario debe ejecutar y pegar outputs)
`
---

**2026-02-09 20:47:14 -05:00 (America/Guayaquil)**
Sprint: SPR-002
Estado resultante: DONE/APROBADO
Resumen:
- Validación completa de scaffold backend/frontend
- npm install y Vite dev OK
- Healthcheck y ping OK

Evidencia:
- Comandos ejecutados por el usuario:
  - cd backend && ./mvnw test
  - cd backend && ./mvnw spring-boot:run
  - curl http://localhost:8080/actuator/health
  - curl http://localhost:8080/api/ping
  - cd frontend && npm install
  - cd frontend && npm run dev
- Output:
```text
[INFO] BUILD SUCCESS
StatusCode : 200 (actuator/health)
Content    : {"ok":true} (api/ping)
npm install: added 256 packages; 2 moderate vulnerabilities
VITE v5.4.21 ready in 1882 ms (http://localhost:5173/)
```
---

**2026-02-09 20:55:39 -05:00 (America/Guayaquil)**
Sprint: SPR-003
Estado resultante: READY_FOR_VALIDATION
Resumen:
- Flyway configurado y migraciones base creadas
- Seed mínimo: tenant, branch, roles, permisos

Evidencia:
- Comandos ejecutados por el usuario:
  - $env:SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sisferrete
  - $env:SPRING_DATASOURCE_USERNAME=postgres
  - $env:SPRING_DATASOURCE_PASSWORD=<tu_password>
  - cd backend
  - ./mvnw spring-boot:run
- Output:
```text
PEGAR OUTPUT AQUÍ
```
---

**2026-02-09 21:08:15 -05:00 (America/Guayaquil)**
Sprint: SPR-003
Estado resultante: READY_FOR_VALIDATION
Resumen:
- Flyway actualizado para soportar PostgreSQL 17.x
- Requiere reintento de migraciones

Evidencia:
- Comandos ejecutados por el usuario:
  - =jdbc:postgresql://localhost:5432/sisferrete
  - =postgres
  - =<tu_password>
  - cd backend
  - ./mvnw spring-boot:run
- Output:
`	ext
PEGAR OUTPUT AQUÍ
`
---

**2026-02-09 21:11:40 -05:00 (America/Guayaquil)**
Sprint: SPR-003
Estado resultante: DONE/APROBADO
Resumen:
- Flyway ejecutó V1 y V2 correctamente en PostgreSQL 17.7
- Seed mínimo aplicado

Evidencia:
- Comandos ejecutados por el usuario:
  - `$env:SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/sisferrete`
  - `$env:SPRING_DATASOURCE_USERNAME=postgres`
  - `$env:SPRING_DATASOURCE_PASSWORD=********`
  - `cd backend`
  - `./mvnw spring-boot:run`
- Output:
```text
Migrating schema "public" to version "1 - platform base"
Migrating schema "public" to version "2 - platform seed"
Successfully applied 2 migrations to schema "public"
BUILD SUCCESS
```
---

**2026-02-09 22:27:26 -05:00 (America/Guayaquil)**
Sprint: SPR-005
Estado resultante: READY_FOR_VALIDATION
Resumen:
- Auditoría base + hook central (AuditService)
- Migración V4__audit_events_columns.sql
- Eventos AUTH_* en login/refresh + access denied
- Script smoke sprint5.ps1

Evidencia:
- Comandos ejecutados por el usuario:
  - `$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/sisferrete"`
  - `$env:SPRING_DATASOURCE_USERNAME="postgres"`
  - `$env:SPRING_DATASOURCE_PASSWORD="TU_PASSWORD_REAL"`
  - `$env:SISFERRETE_ADMIN_EMAIL="admin@demo.com"`
  - `$env:SISFERRETE_ADMIN_PASSWORD="TU_PASSWORD_REAL"`
  - `cd backend`
  - `./mvnw spring-boot:run`
  - `cd ..`
  - `pwsh -ExecutionPolicy Bypass -File .\scripts\smoke\sprint5.ps1`
  - `psql -U postgres -d sisferrete -c "select action_code, created_at from audit_events order by created_at desc limit 10;"`
- Output:
```text
PEGAR OUTPUT AQUÍ
```
<!-- EOF -->
