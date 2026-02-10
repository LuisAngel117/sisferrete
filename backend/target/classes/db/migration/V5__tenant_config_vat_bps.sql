CREATE TABLE IF NOT EXISTS tenant_config (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
  vat_rate numeric(5, 2) NOT NULL DEFAULT 15.00,
  vat_rate_bps integer NOT NULL DEFAULT 1500,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

ALTER TABLE tenant_config
  ADD COLUMN IF NOT EXISTS vat_rate_bps integer;

UPDATE tenant_config
SET vat_rate_bps = round(vat_rate * 100)
WHERE vat_rate_bps IS NULL;

UPDATE tenant_config
SET vat_rate_bps = 1500
WHERE vat_rate_bps IS NULL;

ALTER TABLE tenant_config
  ALTER COLUMN vat_rate_bps SET DEFAULT 1500,
  ALTER COLUMN vat_rate_bps SET NOT NULL;

INSERT INTO permissions (code, name)
VALUES
  ('CONFIG_VAT_EDIT', 'CONFIG_VAT_EDIT'),
  ('CONFIG_VIEW', 'CONFIG_VIEW')
ON CONFLICT (code) DO NOTHING;

WITH perm AS (
  SELECT id FROM permissions WHERE code IN ('CONFIG_VAT_EDIT', 'CONFIG_VIEW')
),
roles AS (
  SELECT id FROM roles WHERE code IN ('SUPERADMIN', 'ADMIN')
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, perm.id
FROM roles
CROSS JOIN perm
ON CONFLICT DO NOTHING;
