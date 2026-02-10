package com.sisferrete.auth;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepository {
  private final JdbcTemplate jdbc;
  private final SecureRandom secureRandom = new SecureRandom();

  public RefreshTokenRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public String createToken(UUID tenantId, UUID userId, Duration ttl) {
    String token = generateToken();
    Instant now = Instant.now();
    Instant expiresAt = now.plus(ttl);
    String sql = """
        insert into refresh_tokens (id, tenant_id, user_id, token, expires_at, created_at)
        values (?, ?, ?, ?, ?, ?)
        """;
    jdbc.update(sql, UUID.randomUUID(), tenantId, userId, token, expiresAt, now);
    return token;
  }

  public Optional<RefreshTokenRecord> findValid(String token) {
    String sql = """
        select id, tenant_id, user_id, token, expires_at, revoked_at, created_at
        from refresh_tokens
        where token = ?
          and revoked_at is null
          and expires_at > now()
        """;
    List<RefreshTokenRecord> tokens = jdbc.query(sql, new RefreshTokenMapper(), token);
    if (tokens.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(tokens.get(0));
  }

  public void revokeToken(UUID tokenId) {
    String sql = "update refresh_tokens set revoked_at = now() where id = ?";
    jdbc.update(sql, tokenId);
  }

  private String generateToken() {
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private static class RefreshTokenMapper implements RowMapper<RefreshTokenRecord> {
    @Override
    public RefreshTokenRecord mapRow(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
      return new RefreshTokenRecord(
          rs.getObject("id", UUID.class),
          rs.getObject("tenant_id", UUID.class),
          rs.getObject("user_id", UUID.class),
          rs.getString("token"),
          rs.getTimestamp("expires_at").toInstant(),
          rs.getTimestamp("revoked_at") != null ? rs.getTimestamp("revoked_at").toInstant() : null,
          rs.getTimestamp("created_at").toInstant()
      );
    }
  }
}