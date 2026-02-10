# ADR-0001 — Stack base (Java/Spring + React + Postgres)
**Estado:** ACEPTADO (no editar; nuevos ADR para cambios)  
**Fecha:** 2026-02-09  

---

## Contexto
Se requiere un stack moderno, mantenible, vendible y probado en local-first.

## Decisión
- Backend: Java 21 + Spring Boot 3.x
- DB: PostgreSQL local + migraciones
- Frontend: React + Vite + TypeScript + Tailwind + shadcn/ui
- Auth: JWT + refresh token; 2FA TOTP opcional

## Consecuencias
- Permite desarrollo rápido y ordenado.
- Migraciones garantizan reproducibilidad.
- UI moderna y productizable.

<!-- EOF -->
