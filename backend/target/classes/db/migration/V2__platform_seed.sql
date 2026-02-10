INSERT INTO permissions (code, name)
VALUES
  ('config.iva.edit', 'config.iva.edit'),
  ('config.document_sequences.edit', 'config.document_sequences.edit'),
  ('config.security.policies.edit', 'config.security.policies.edit'),
  ('users.manage', 'users.manage'),
  ('roles.manage', 'roles.manage'),
  ('permissions.manage', 'permissions.manage'),
  ('branches.manage', 'branches.manage'),
  ('prices.edit', 'prices.edit'),
  ('prices.discount.manual', 'prices.discount.manual'),
  ('prices.sell_below_cost.authorize', 'prices.sell_below_cost.authorize'),
  ('costs.edit', 'costs.edit'),
  ('stock.adjust.create', 'stock.adjust.create'),
  ('stock.adjust.approve', 'stock.adjust.approve'),
  ('stock.transfer.create', 'stock.transfer.create'),
  ('stock.transfer.receive', 'stock.transfer.receive'),
  ('cash.open', 'cash.open'),
  ('cash.close', 'cash.close'),
  ('cash.adjust', 'cash.adjust'),
  ('sales.void', 'sales.void'),
  ('sales.refund', 'sales.refund'),
  ('sales.reprint', 'sales.reprint'),
  ('audit.view', 'audit.view'),
  ('reports.view_sensitive', 'reports.view_sensitive'),
  ('reports.export', 'reports.export'),
  ('ecom.catalog.manage', 'ecom.catalog.manage'),
  ('ecom.orders.manage', 'ecom.orders.manage');

WITH tenant AS (
  INSERT INTO tenants (name, code)
  VALUES ('Ferreteria', 'FERRETERIA')
  RETURNING id
),
branch AS (
  INSERT INTO branches (tenant_id, name, code)
  SELECT id, 'Matriz', 'MATRIZ'
  FROM tenant
  RETURNING id, tenant_id
),
roles AS (
  INSERT INTO roles (tenant_id, code, name, description)
  SELECT id, 'SUPERADMIN', 'Superadmin', 'Acceso total' FROM tenant
  UNION ALL
  SELECT id, 'ADMIN', 'Admin', 'Administración' FROM tenant
  UNION ALL
  SELECT id, 'CAJERO', 'Cajero', 'Operación POS' FROM tenant
  RETURNING id, tenant_id
)
INSERT INTO tenant_config (tenant_id, vat_rate)
SELECT id, 12.00 FROM tenant;