# ADR-0003 — Tipos para dinero y cantidades (exactitud)
**Estado:** ACEPTADO (no editar; nuevos ADR para cambios)  
**Fecha:** 2026-02-09  

---

## Contexto
POS e inventario requieren exactitud; floats rompen montos y cantidades.

## Decisión
- Dinero: decimal exacto, 2 decimales (centavos).
- Cantidades:
  - UNIDAD/CAJA/PAQUETE: enteros
  - METRO/KILO/LITRO: decimales con escala definida por UoM
- Validación estricta en POS, compras e inventario.

## Consecuencias
- Reportes y márgenes confiables.
- Menos errores en caja.

<!-- EOF -->
