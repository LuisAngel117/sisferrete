package com.sisferrete.platform.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AuditRepository {
  private final JdbcTemplate jdbc;
  private final ObjectMapper objectMapper;

  public AuditRepository(JdbcTemplate jdbc, ObjectMapper objectMapper) {
    this.jdbc = jdbc;
    this.objectMapper = objectMapper;
  }

  public void insert(AuditEvent event) {
    String sql = """
        insert into audit_events (
          id,
          tenant_id,
          branch_id,
          actor_user_id,
          actor_email,
          action_code,
          entity_type,
          entity_id,
          summary,
          before_state,
          after_state,
          metadata,
          ip_address,
          user_agent,
          created_at
        ) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?::jsonb, ?, ?, ?)
        """;

    jdbc.update(
        sql,
        event.id(),
        event.tenantId(),
        event.branchId(),
        event.actorUserId(),
        event.actorEmail(),
        event.actionCode(),
        event.entityType(),
        event.entityId(),
        event.summary(),
        toJson(event.beforeState()),
        toJson(event.afterState()),
        toJson(event.metadata()),
        event.ipAddress(),
        event.userAgent(),
        event.createdAt() != null ? event.createdAt() : Instant.now()
    );
  }

  private String toJson(Object value) {
    if (value == null) {
      return null;
    }
    if (value instanceof String text) {
      return text;
    }
    try {
      return objectMapper.writeValueAsString(value);
    } catch (JsonProcessingException ex) {
      throw new IllegalStateException("No se pudo serializar JSON para auditoría", ex);
    }
  }
}
