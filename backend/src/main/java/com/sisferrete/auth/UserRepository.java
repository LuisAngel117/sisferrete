package com.sisferrete.auth;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
  private final JdbcTemplate jdbc;

  public UserRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public Optional<UserAccount> findByEmail(String email) {
    String sql = """
        select id, tenant_id, email, full_name, password_hash, two_factor_enabled, two_factor_secret, is_active
        from users
        where lower(email) = lower(?)
        """;
    List<UserAccount> users = jdbc.query(sql, new UserMapper(), email);
    if (users.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(users.get(0));
  }

  public Optional<UserAccount> findById(UUID userId) {
    String sql = """
        select id, tenant_id, email, full_name, password_hash, two_factor_enabled, two_factor_secret, is_active
        from users
        where id = ?
        """;
    List<UserAccount> users = jdbc.query(sql, new UserMapper(), userId);
    if (users.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(users.get(0));
  }

  public List<String> findRoles(UUID userId) {
    String sql = """
        select r.code
        from roles r
        join user_roles ur on ur.role_id = r.id
        where ur.user_id = ?
        order by r.code
        """;
    return jdbc.queryForList(sql, String.class, userId);
  }

  public List<String> findPermissions(UUID userId) {
    String sql = """
        select distinct p.code
        from permissions p
        join role_permissions rp on rp.permission_id = p.id
        join user_roles ur on ur.role_id = rp.role_id
        where ur.user_id = ?
        order by p.code
        """;
    return jdbc.queryForList(sql, String.class, userId);
  }

  public List<UUID> findBranchAccess(UUID userId) {
    String sql = """
        select branch_id
        from user_branch_access
        where user_id = ?
        order by branch_id
        """;
    return jdbc.queryForList(sql, UUID.class, userId);
  }

  public Optional<UUID> findTenantIdByCode(String code) {
    String sql = "select id from tenants where code = ?";
    List<UUID> ids = jdbc.queryForList(sql, UUID.class, code);
    if (ids.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(ids.get(0));
  }

  public Optional<UUID> findBranchIdByCode(UUID tenantId, String code) {
    String sql = "select id from branches where tenant_id = ? and code = ?";
    List<UUID> ids = jdbc.queryForList(sql, UUID.class, tenantId, code);
    if (ids.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(ids.get(0));
  }

  public Optional<UUID> findRoleIdByCode(UUID tenantId, String code) {
    String sql = "select id from roles where tenant_id = ? and code = ?";
    List<UUID> ids = jdbc.queryForList(sql, UUID.class, tenantId, code);
    if (ids.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(ids.get(0));
  }

  public void insertUser(UUID userId, UUID tenantId, String email, String fullName, String passwordHash) {
    String sql = """
        insert into users (id, tenant_id, email, password_hash, full_name, is_active, two_factor_enabled)
        values (?, ?, ?, ?, ?, true, false)
        """;
    jdbc.update(sql, userId, tenantId, email, passwordHash, fullName);
  }

  public void insertUserRole(UUID tenantId, UUID userId, UUID roleId) {
    String sql = """
        insert into user_roles (id, tenant_id, user_id, role_id, scope_branch_id)
        values (?, ?, ?, ?, null)
        """;
    jdbc.update(sql, UUID.randomUUID(), tenantId, userId, roleId);
  }

  public void insertUserBranchAccess(UUID tenantId, UUID userId, UUID branchId) {
    String sql = """
        insert into user_branch_access (id, tenant_id, user_id, branch_id)
        values (?, ?, ?, ?)
        on conflict (user_id, branch_id) do nothing
        """;
    jdbc.update(sql, UUID.randomUUID(), tenantId, userId, branchId);
  }

  private static class UserMapper implements RowMapper<UserAccount> {
    @Override
    public UserAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new UserAccount(
          rs.getObject("id", UUID.class),
          rs.getObject("tenant_id", UUID.class),
          rs.getString("email"),
          rs.getString("full_name"),
          rs.getString("password_hash"),
          rs.getBoolean("two_factor_enabled"),
          rs.getString("two_factor_secret"),
          rs.getBoolean("is_active")
      );
    }
  }
}