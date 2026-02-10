# 07 — UX/UI (PARTE A) — Navegación, roles y POS (caja)
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  
**Prioridad:** POS/caja rápido. UI en Español.  

---

## 1) Principios UX
- POS “teclado-first”: minimizar mouse.
- Búsqueda rápida: nombre/SKU/barcode con autocompletado.
- Validaciones claras: no permitir stock negativo ni acciones sin permiso.
- Confirmaciones solo cuando sea riesgoso (anular, devolver, bajo costo, etc.).

---

## 2) Layout general (app privada)
### 2.1 Shell
- Top bar:
  - selector de sucursal (si usuario tiene varias)
  - usuario (perfil, logout, 2FA)
- Side nav (según permisos):
  - POS
  - Proformas
  - Productos
  - Inventario
  - Compras
  - Clientes
  - Proveedores
  - Reportes
  - Auditoría
  - Configuración (solo Admin/Superadmin)
  - E-commerce (gestión pedidos y catálogo)

### 2.2 Permisos visibles
- Acciones sin permiso:
  - botón deshabilitado + tooltip “Requiere permiso X”.
- Acciones sensibles:
  - requieren confirmación o supervisor override.

---

## 3) Flujos de autenticación
- Login: email + contraseña.
- Si 2FA activo:
  - solicitar código TOTP.
- Recuperación contraseña: flujo TBD (cuando se implemente).

---

## 4) POS (Pantalla de Caja)
### 4.1 Objetivo de pantalla
Permitir vender en 10–30 segundos en escenarios comunes.

### 4.2 Componentes principales
- Barra de búsqueda/scan (focus automático siempre)
- Lista de líneas de venta
- Panel resumen: subtotal, impuestos, total
- Panel acciones: proforma, cobrar, cancelar

### 4.3 Flujo feliz (venta)
1) Usuario abre POS.
2) Escanea barcode o escribe nombre/SKU.
3) Selecciona producto (si hay variantes, elige variante).
4) Ingresa cantidad:
   - si UoM permite decimal: input decimal con escala
   - si no: solo enteros
5) (Opcional) aplica descuento:
   - si descuento manual: requiere permiso o supervisor override.
6) “Cobrar” → modal de pago:
   - métodos según configuración (mínimo: efectivo/transferencia/tarjeta)
7) Confirmar → imprimir ticket.

### 4.4 Supervisor override (cuando aplica)
Casos:
- descuento manual sin permiso
- venta bajo costo (o margen)
- anular venta
- devolución

UX:
- modal solicita credencial supervisor (email+password o PIN, definido al implementar)
- registra auditoría con operador y supervisor.

### 4.5 Estados y errores
- Sin stock suficiente → error claro “Stock insuficiente. Disponible: X”.
- Producto inactivo → no vendible.
- Sin branch seleccionado → bloquear y pedir selección.

---

## 5) Proformas (cotizaciones)
### 5.1 Crear proforma desde POS o módulo
- Similar a venta pero sin cobro.
- Exportable/compartible (WhatsApp: enlace o PDF/imagen, definido luego).
- Convertir a venta:
  - “Convertir” → crea venta con mismas líneas
  - sin reingresar productos

---

## 6) Pantallas mínimas (módulos privados)
- Productos:
  - listado con filtros (categoría/marca/atributos)
  - crear/editar producto + variantes + empaques + imágenes
- Inventario:
  - stock por sucursal
  - transferencias
  - conteos cíclicos
  - ajustes (con aprobación)
- Compras:
  - registrar compra
  - recepción parcial
- Clientes:
  - listado
  - estado de cuenta
  - abonos
- Reportes:
  - ventas, margen, rotación, stock bajo, etc.
- Auditoría:
  - búsqueda por fecha, usuario, acción, entidad

<!-- EOF -->
