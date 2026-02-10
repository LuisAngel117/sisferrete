CREATE TABLE IF NOT EXISTS product_packagings (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
  product_id uuid NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
  variant_id uuid,
  sale_uom_id uuid NOT NULL REFERENCES units_of_measure(id) ON DELETE RESTRICT,
  base_uom_id uuid NOT NULL REFERENCES units_of_measure(id) ON DELETE RESTRICT,
  base_units_per_sale_unit numeric(18,6) NOT NULL,
  barcode text,
  is_default_for_sale boolean NOT NULL DEFAULT false,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now(),
  CONSTRAINT ck_product_packagings_base_units_pos CHECK (base_units_per_sale_unit > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_product_packagings_tenant_barcode
  ON product_packagings(tenant_id, barcode)
  WHERE barcode IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_product_packagings_tenant_product_sale_uom
  ON product_packagings(tenant_id, product_id, sale_uom_id)
  WHERE variant_id IS NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_product_packagings_tenant_product_variant_sale_uom
  ON product_packagings(tenant_id, product_id, variant_id, sale_uom_id)
  WHERE variant_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_product_packagings_tenant_product
  ON product_packagings(tenant_id, product_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_product_variants_id_product
  ON product_variants(id, product_id);

ALTER TABLE product_packagings
  ADD CONSTRAINT fk_product_packagings_variant_product
  FOREIGN KEY (variant_id, product_id)
  REFERENCES product_variants(id, product_id)
  ON DELETE RESTRICT;
