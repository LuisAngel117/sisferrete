CREATE TABLE IF NOT EXISTS product_variants (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
  product_id uuid NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
  name text NOT NULL,
  barcode text,
  attributes jsonb,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_product_variants_tenant_barcode
  ON product_variants(tenant_id, barcode)
  WHERE barcode IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_product_variants_tenant_product
  ON product_variants(tenant_id, product_id);
