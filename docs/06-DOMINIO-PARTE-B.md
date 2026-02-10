# 06 — DOMINIO (PARTE B) — Inventario, compras, ventas, e-commerce y fiscal opcional
**Estado:** BLOQUEADO (no editar; cambios solo por RFC/ADR/CHANGELOG)  

---

## 1) Inventario
### 1.1 Location (pasillo/estante)
**Tabla:** `stock_locations`  
**Campos:**
- `id`, `tenant_id`, `branch_id`
- `code` (string) — ej: `P1-E3`
- `description` (string)

### 1.2 StockBalance
**Tabla:** `stock_balances`  
**Campos:**
- `id`, `tenant_id`, `branch_id`
- `product_id` / `variant_id`
- `packaging_id` (nullable)
- `quantity_on_hand` (decimal exacto)
- `quantity_reserved` (decimal exacto)
- `reorder_min` (decimal, nullable)
- `reorder_max` (decimal, nullable)

**Reglas:**
- `quantity_on_hand - quantity_reserved` = disponible.
- Nunca permitir que disponible sea negativo al confirmar operaciones.

### 1.3 StockMovement (kardex)
**Tabla:** `stock_movements`  
**Campos:**
- `id`, `tenant_id`, `branch_id`
- `movement_type` (purchase/sale/adjustment/transfer_in/transfer_out/reservation_release)
- `reference_type`, `reference_id`
- `product_id` / `variant_id`
- `quantity_delta` (decimal)
- `uom_id`
- `unit_cost` (decimal, nullable) — para costeo
- `created_at`, `created_by`

### 1.4 CycleCount (conteos cíclicos)
**Tabla:** `cycle_counts`  
**Campos:**
- `id`, `tenant_id`, `branch_id`
- `status` (draft/in_progress/closed)
- `scheduled_at`, `started_at`, `closed_at`
- `created_by`

**Tabla:** `cycle_count_lines`
- `cycle_count_id`
- `product_id` / `variant_id`
- `expected_qty`
- `counted_qty`
- `difference_qty`
- `notes`

**Reglas:**
- Diferencias se reportan y pueden generar ajuste (con aprobación).

### 1.5 StockAdjustment
**Tabla:** `stock_adjustments`  
**Campos:**
- `id`, `tenant_id`, `branch_id`
- `reason_code` (string)
- `status` (pending/approved/rejected/applied)
- `requested_by`, `requested_at`
- `approved_by`, `approved_at` (nullable)
- `notes`

**Tabla:** `stock_adjustment_lines`
- `adjustment_id`
- `product_id` / `variant_id`
- `quantity_delta`
- `uom_id`

**Reglas:**
- Crear ajuste requiere `stock.adjust.create`
- Aprobar requiere `stock.adjust.approve`
- Aplicar ajuste produce movimientos y auditoría.

### 1.6 StockTransfer
**Tabla:** `stock_transfers`  
**Campos:**
- `id`, `tenant_id`
- `from_branch_id`, `to_branch_id`
- `status` (created/in_transit/partially_received/received/cancelled)
- `created_by`, `created_at`

**Tabla:** `stock_transfer_lines`
- `transfer_id`
- `product_id` / `variant_id`
- `qty_sent`
- `qty_received`

**Reglas:**
- Recepción parcial permitida.
- Movimientos out/in se registran.

---

## 2) Compras y costos
### 2.1 Supplier
**Tabla:** `suppliers`  
**Campos:** `id`, `tenant_id`, `name`, `tax_id`, `phone`, `email`, `is_active`

### 2.2 Purchase
**Tabla:** `purchases`  
**Campos:**
- `id`, `tenant_id`, `branch_id`
- `supplier_id`
- `invoice_number` (string, nullable)
- `status` (draft/partially_received/received/cancelled)
- `subtotal`, `tax`, `total` (decimals)
- `created_by`, `created_at`

**Tabla:** `purchase_lines`
- `purchase_id`
- `product_id` / `variant_id`
- `quantity`
- `uom_id`
- `unit_cost`
- `line_total`

### 2.3 PurchaseExpense (prorrateo)
**Tabla:** `purchase_expenses`
- `purchase_id`
- `type` (freight/other)
- `amount`

**Reglas:**
- Gastos se prorratean al costo al recibir.

### 2.4 WeightedAverageCost (costo promedio)
**Tabla sugerida:** `product_costs`
- `tenant_id`, `branch_id` (si costo por sucursal) o solo tenant (si global)
- `product_id`/`variant_id`
- `avg_cost`

**Regla:**
- Método: promedio ponderado (ADR-0004).
- Se recalcula con recepciones.

---

## 3) Ventas / POS / Caja
### 3.1 Customer
**Tabla:** `customers`
- `id`, `tenant_id`
- `name`
- `tax_id` (nullable)
- `phone`, `email` (nullable)
- `is_active`

### 3.2 Quote (Proforma/Cotización)
**Tabla:** `quotes`
- `id`, `tenant_id`, `branch_id`
- `customer_id` (nullable)
- `status` (draft/sent/converted/cancelled)
- `price_list_id`
- `subtotal`, `tax`, `total`
- `created_by`, `created_at`

**Tabla:** `quote_lines`
- `quote_id`
- `product_id`/`variant_id`
- `quantity`
- `unit_price`
- `discount_amount` (nullable)
- `line_total`

### 3.3 Sale
**Tabla:** `sales`
- `id`, `tenant_id`, `branch_id`
- `customer_id` (nullable)
- `quote_id` (nullable)
- `status` (draft/paid/voided/refunded)
- `price_list_id`
- `subtotal`, `tax`, `total`
- `created_by`, `created_at`

**Tabla:** `sale_lines`
- `sale_id`
- `product_id`/`variant_id`
- `quantity`
- `unit_price`
- `discount_amount`
- `line_total`
- `cost_snapshot` (decimal, nullable) — para auditoría/reportes

**Reglas:**
- Convertir proforma a venta sin reingreso.
- Venta bajo costo requiere autorización.

### 3.4 Payment
**Tabla:** `payments`
- `id`, `tenant_id`, `branch_id`
- `sale_id`
- `method` (cash/card/transfer/mixed)
- `amount`
- `reference` (nullable)
- `created_at`

### 3.5 CashSession (caja)
**Tabla:** `cash_sessions`
- `id`, `tenant_id`, `branch_id`
- `status` (open/closed)
- `opened_by`, `opened_at`
- `closed_by`, `closed_at`
- `opening_amount` (decimal)
- `closing_amount` (decimal, nullable)

**Reglas:**
- Apertura/cierre configurable.
- Diferencias se auditan.

---

## 4) CxC / CxP (mínimo)
### 4.1 AccountsReceivable
**Tabla:** `ar_entries`
- `id`, `tenant_id`, `customer_id`
- `sale_id`
- `amount_due`
- `amount_paid`
- `status` (open/closed)

### 4.2 AccountsPayable
**Tabla:** `ap_entries`
- `id`, `tenant_id`, `supplier_id`
- `purchase_id`
- `amount_due`
- `amount_paid`
- `status`

---

## 5) E-commerce
### 5.1 WebOrder
**Tabla:** `web_orders`
- `id`, `tenant_id`, `branch_id` (branch fulfillment)
- `status` (new/confirmed/preparing/delivered/cancelled)
- `customer_name`, `customer_phone`, `customer_email` (nullable)
- `delivery_method` (pickup/delivery)
- `payment_method` (cash_on_delivery/transfer)
- `subtotal`, `tax`, `total`
- `created_at`

**Tabla:** `web_order_lines`
- `web_order_id`
- `product_id`/`variant_id`
- `quantity`
- `unit_price`
- `line_total`

**Reglas:**
- Al crear pedido: reservar stock (`quantity_reserved`).
- Al cancelar: liberar reserva.

---

## 6) Fiscal opcional (fase online)
> Se define “contrato” de datos sin implementar aún.

### 6.1 DocumentSequence
**Tabla:** `document_sequences`
- `id`, `tenant_id`
- `doc_type` (invoice/credit_note/etc.)
- `current_number`
- `series` (nullable)
- `is_active`

**Regla:**
- Cambios protegidos por `config.document_sequences.edit` y auditados.

---

<!-- EOF -->
