CREATE TABLE IF NOT EXISTS categories (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
  code text NOT NULL,
  name text NOT NULL,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_categories_tenant_code ON categories(tenant_id, code);

CREATE TABLE IF NOT EXISTS brands (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
  code text NOT NULL,
  name text NOT NULL,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_brands_tenant_code ON brands(tenant_id, code);

CREATE TABLE IF NOT EXISTS units_of_measure (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
  code text NOT NULL,
  name text NOT NULL,
  allows_decimals boolean NOT NULL DEFAULT false,
  is_active boolean NOT NULL DEFAULT true,
  created_at timestamptz NOT NULL DEFAULT now(),
  updated_at timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_units_of_measure_tenant_code ON units_of_measure(tenant_id, code);

WITH base_uoms(code, name, allows_decimals) AS (
  VALUES
    ('UNIDAD', 'Unidad', false),
    ('CAJA', 'Caja', false),
    ('PAQUETE', 'Paquete', false),
    ('METRO', 'Metro', true),
    ('KILOGRAMO', 'Kilogramo', true),
    ('LITRO', 'Litro', true)
)
INSERT INTO units_of_measure (tenant_id, code, name, allows_decimals, is_active)
SELECT tenants.id, base_uoms.code, base_uoms.name, base_uoms.allows_decimals, true
FROM tenants
CROSS JOIN base_uoms
ON CONFLICT (tenant_id, code) DO NOTHING;

INSERT INTO permissions (code, name)
VALUES ('CATALOG_MANAGE', 'CATALOG_MANAGE')
ON CONFLICT (code) DO NOTHING;

WITH perm AS (
  SELECT id FROM permissions WHERE code = 'CATALOG_MANAGE'
),
roles AS (
  SELECT id FROM roles WHERE code IN ('ADMIN', 'SUPERADMIN')
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, perm.id
FROM roles
CROSS JOIN perm
ON CONFLICT DO NOTHING;
