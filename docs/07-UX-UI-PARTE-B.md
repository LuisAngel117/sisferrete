# 07 — UX/UI (PARTE B) — E-commerce público + backoffice pedidos
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  

---

## 1) E-commerce público (sin login al inicio)
### 1.1 Navegación pública
- Home / categorías
- Listado de productos con filtros
- Detalle de producto (imágenes, variantes, precio, disponibilidad)
- Carrito
- Checkout (pedido)

### 1.2 Checkout inicial (sin pasarela)
- Datos mínimos:
  - nombre
  - teléfono
  - email opcional
  - método entrega (retirar / delivery)
  - método pago (contra entrega / transferencia)
- Confirmar pedido:
  - crea `web_order`
  - reserva stock

### 1.3 Stock y disponibilidad
- Mostrar disponibilidad estimada (no exponer números exactos si se decide).
- En checkout, validar stock disponible real.

---

## 2) Backoffice de pedidos (privado)
### 2.1 Lista de pedidos
- filtros por estado y fecha
- acciones por estado:
  - confirmar
  - preparar
  - marcar entregado
  - cancelar (libera reserva)

### 2.2 Reserva stock (regla UX)
- Al crear pedido: reserva.
- Si se cancela: liberar.
- Si se convierte a venta POS: migrar reserva a salida de stock.

---

## 3) Gestión de catálogo para e-commerce
- habilitar/deshabilitar “visible en web”
- imágenes obligatorias (ideal)
- descripciones más amigables (web)

---

## 4) Reportes UX (exportación)
- Exportar a PDF/Excel.
- Filtros guardables (opcional futuro).

---

## 5) Accesibilidad y performance
- Performance:
  - listados paginados
  - búsqueda con debounce
- Accesibilidad:
  - foco visible
  - labels en inputs
  - navegación por teclado en POS

<!-- EOF -->
