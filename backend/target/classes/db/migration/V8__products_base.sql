CREATE TABLE IF NOT EXISTS products (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
  name text NOT NULL,
  sku text,
  barcode text,
  category_id uuid REFERENCES categories(id) ON DELETE RESTRICT,
  brand_id uuid REFERENCES brands(id) ON DELETE RESTRICT,
  uom_id uuid NOT NULL REFERENCES units_of_measure(id) ON DELETE RESTRICT,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT ck_products_sku_or_barcode CHECK (sku IS NOT NULL OR barcode IS NOT NULL)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_products_tenant_sku
  ON products(tenant_id, sku)
  WHERE sku IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_products_tenant_barcode
  ON products(tenant_id, barcode)
  WHERE barcode IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_products_tenant_name ON products(tenant_id, name);
