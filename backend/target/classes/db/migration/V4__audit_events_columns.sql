ALTER TABLE audit_events
  ADD COLUMN IF NOT EXISTS actor_email text,
  ADD COLUMN IF NOT EXISTS summary text,
  ADD COLUMN IF NOT EXISTS before_state jsonb,
  ADD COLUMN IF NOT EXISTS after_state jsonb,
  ADD COLUMN IF NOT EXISTS metadata jsonb,
  ADD COLUMN IF NOT EXISTS ip_address text,
  ADD COLUMN IF NOT EXISTS user_agent text;

CREATE INDEX IF NOT EXISTS ix_audit_events_tenant_created_at_desc
  ON audit_events (tenant_id, created_at desc);

CREATE INDEX IF NOT EXISTS ix_audit_events_tenant_action_created_at
  ON audit_events (tenant_id, action_code, created_at desc);
