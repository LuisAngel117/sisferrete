CREATE TABLE IF NOT EXISTS user_branch_access (
  id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
  tenant_id uuid NOT NULL REFERENCES tenants(id) ON DELETE RESTRICT,
  user_id uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  branch_id uuid NOT NULL REFERENCES branches(id) ON DELETE RESTRICT,
  created_at timestamptz NOT NULL DEFAULT now(),
  UNIQUE (user_id, branch_id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_tenant_email ON users(tenant_id, email);
CREATE UNIQUE INDEX IF NOT EXISTS ux_roles_tenant_code ON roles(tenant_id, code);
CREATE INDEX IF NOT EXISTS ix_user_roles_user ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS ix_user_branch_access_user ON user_branch_access(user_id);

INSERT INTO permissions (code, name)
VALUES
  ('IAM_MANAGE', 'IAM_MANAGE'),
  ('AUDIT_VIEW', 'AUDIT_VIEW'),
  ('CONFIG_VAT_EDIT', 'CONFIG_VAT_EDIT')
ON CONFLICT (code) DO NOTHING;

WITH perms AS (
  SELECT id FROM permissions WHERE code IN ('IAM_MANAGE', 'AUDIT_VIEW', 'CONFIG_VAT_EDIT')
),
roles AS (
  SELECT id FROM roles WHERE code IN ('SUPERADMIN', 'ADMIN')
)
INSERT INTO role_permissions (role_id, permission_id)
SELECT roles.id, perms.id
FROM roles
CROSS JOIN perms
ON CONFLICT DO NOTHING;
