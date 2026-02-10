package com.sisferrete.platform.iam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class IamRepository {
  private final JdbcTemplate jdbc;

  public IamRepository(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  public List<UserRecord> listUsers(UUID tenantId) {
    String sql = """
        select id, tenant_id, email, full_name, is_active
        from users
        where tenant_id = ?
        order by email
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new UserRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("tenant_id", UUID.class),
            rs.getString("email"),
            rs.getString("full_name"),
            rs.getBoolean("is_active")
        ), tenantId);
  }

  public Optional<UserRecord> findUser(UUID tenantId, UUID userId) {
    String sql = """
        select id, tenant_id, email, full_name, is_active
        from users
        where tenant_id = ? and id = ?
        """;
    List<UserRecord> users = jdbc.query(sql, (rs, rowNum) ->
        new UserRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("tenant_id", UUID.class),
            rs.getString("email"),
            rs.getString("full_name"),
            rs.getBoolean("is_active")
        ), tenantId, userId);
    if (users.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(users.get(0));
  }

  public Optional<UserRecord> findUserByEmail(UUID tenantId, String email) {
    String sql = """
        select id, tenant_id, email, full_name, is_active
        from users
        where tenant_id = ? and lower(email) = lower(?)
        """;
    List<UserRecord> users = jdbc.query(sql, (rs, rowNum) ->
        new UserRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("tenant_id", UUID.class),
            rs.getString("email"),
            rs.getString("full_name"),
            rs.getBoolean("is_active")
        ), tenantId, email);
    if (users.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(users.get(0));
  }

  public UUID insertUser(UUID tenantId, String email, String fullName, String passwordHash, boolean isActive) {
    UUID userId = UUID.randomUUID();
    String sql = """
        insert into users (id, tenant_id, email, password_hash, full_name, is_active)
        values (?, ?, ?, ?, ?, ?)
        """;
    jdbc.update(sql, userId, tenantId, email, passwordHash, fullName, isActive);
    return userId;
  }

  public int updateUser(UUID tenantId, UUID userId, String fullName, boolean isActive) {
    String sql = """
        update users
        set full_name = ?, is_active = ?
        where tenant_id = ? and id = ?
        """;
    return jdbc.update(sql, fullName, isActive, tenantId, userId);
  }

  public List<String> findUserRoles(UUID userId) {
    String sql = """
        select r.code
        from roles r
        join user_roles ur on ur.role_id = r.id
        where ur.user_id = ?
        order by r.code
        """;
    return jdbc.queryForList(sql, String.class, userId);
  }

  public List<UUID> findUserBranches(UUID userId) {
    String sql = """
        select branch_id
        from user_branch_access
        where user_id = ?
        order by branch_id
        """;
    return jdbc.queryForList(sql, UUID.class, userId);
  }

  public void replaceUserRoles(UUID tenantId, UUID userId, List<UUID> roleIds) {
    String deleteSql = "delete from user_roles where tenant_id = ? and user_id = ?";
    jdbc.update(deleteSql, tenantId, userId);
    String insertSql = """
        insert into user_roles (id, tenant_id, user_id, role_id, scope_branch_id)
        values (?, ?, ?, ?, null)
        """;
    for (UUID roleId : roleIds) {
      jdbc.update(insertSql, UUID.randomUUID(), tenantId, userId, roleId);
    }
  }

  public void replaceUserBranches(UUID tenantId, UUID userId, List<UUID> branchIds) {
    String deleteSql = "delete from user_branch_access where tenant_id = ? and user_id = ?";
    jdbc.update(deleteSql, tenantId, userId);
    String insertSql = """
        insert into user_branch_access (id, tenant_id, user_id, branch_id)
        values (?, ?, ?, ?)
        """;
    for (UUID branchId : branchIds) {
      jdbc.update(insertSql, UUID.randomUUID(), tenantId, userId, branchId);
    }
  }

  public List<RoleRecord> listRoles(UUID tenantId) {
    String sql = """
        select id, tenant_id, code, name, description
        from roles
        where tenant_id = ?
        order by code
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new RoleRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("tenant_id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description")
        ), tenantId);
  }

  public Optional<RoleRecord> findRole(UUID tenantId, UUID roleId) {
    String sql = """
        select id, tenant_id, code, name, description
        from roles
        where tenant_id = ? and id = ?
        """;
    List<RoleRecord> roles = jdbc.query(sql, (rs, rowNum) ->
        new RoleRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("tenant_id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description")
        ), tenantId, roleId);
    if (roles.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(roles.get(0));
  }

  public Optional<RoleRecord> findRoleByCode(UUID tenantId, String code) {
    String sql = """
        select id, tenant_id, code, name, description
        from roles
        where tenant_id = ? and code = ?
        """;
    List<RoleRecord> roles = jdbc.query(sql, (rs, rowNum) ->
        new RoleRecord(
            rs.getObject("id", UUID.class),
            rs.getObject("tenant_id", UUID.class),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description")
        ), tenantId, code);
    if (roles.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(roles.get(0));
  }

  public UUID insertRole(UUID tenantId, String code, String name, String description) {
    UUID roleId = UUID.randomUUID();
    String sql = """
        insert into roles (id, tenant_id, code, name, description)
        values (?, ?, ?, ?, ?)
        """;
    jdbc.update(sql, roleId, tenantId, code, name, description);
    return roleId;
  }

  public int updateRole(UUID tenantId, UUID roleId, String name, String description) {
    String sql = """
        update roles
        set name = ?, description = ?
        where tenant_id = ? and id = ?
        """;
    return jdbc.update(sql, name, description, tenantId, roleId);
  }

  public void replaceRolePermissions(UUID roleId, List<UUID> permissionIds) {
    String deleteSql = "delete from role_permissions where role_id = ?";
    jdbc.update(deleteSql, roleId);
    String insertSql = """
        insert into role_permissions (role_id, permission_id)
        values (?, ?)
        """;
    for (UUID permissionId : permissionIds) {
      jdbc.update(insertSql, roleId, permissionId);
    }
  }

  public List<String> findRolePermissions(UUID roleId) {
    String sql = """
        select p.code
        from permissions p
        join role_permissions rp on rp.permission_id = p.id
        where rp.role_id = ?
        order by p.code
        """;
    return jdbc.queryForList(sql, String.class, roleId);
  }

  public List<PermissionRecord> listPermissions() {
    String sql = """
        select code, name, description
        from permissions
        order by code
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new PermissionRecord(
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description")
        ));
  }

  public Optional<PermissionRecord> findPermissionByCode(String code) {
    String sql = """
        select code, name, description
        from permissions
        where code = ?
        """;
    List<PermissionRecord> permissions = jdbc.query(sql, (rs, rowNum) ->
        new PermissionRecord(
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description")
        ), code);
    if (permissions.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(permissions.get(0));
  }

  public List<BranchRecord> listBranches(UUID tenantId) {
    String sql = """
        select id, code, name
        from branches
        where tenant_id = ?
        order by code
        """;
    return jdbc.query(sql, (rs, rowNum) ->
        new BranchRecord(
            rs.getObject("id", UUID.class),
            rs.getString("code"),
            rs.getString("name")
        ), tenantId);
  }

  public List<UUID> listBranchIds(UUID tenantId) {
    String sql = "select id from branches where tenant_id = ? order by id";
    return jdbc.queryForList(sql, UUID.class, tenantId);
  }

  public List<UUID> findBranchIds(UUID tenantId, List<UUID> branchIds) {
    if (branchIds.isEmpty()) {
      return List.of();
    }
    String sql = """
        select id
        from branches
        where tenant_id = ?
          and id = any(?)
        """;
    return jdbc.query(sql, ps -> {
      ps.setObject(1, tenantId);
      ps.setArray(2, ps.getConnection().createArrayOf("uuid", branchIds.toArray()));
    }, (rs, rowNum) -> rs.getObject("id", UUID.class));
  }

  public List<UUID> findRoleIdsByCodes(UUID tenantId, List<String> roleCodes) {
    if (roleCodes.isEmpty()) {
      return List.of();
    }
    String sql = """
        select id
        from roles
        where tenant_id = ?
          and code = any(?)
        """;
    return jdbc.query(sql, ps -> {
      ps.setObject(1, tenantId);
      ps.setArray(2, ps.getConnection().createArrayOf("text", roleCodes.toArray()));
    }, (rs, rowNum) -> rs.getObject("id", UUID.class));
  }

  public List<UUID> findPermissionIdsByCodes(List<String> permissionCodes) {
    if (permissionCodes.isEmpty()) {
      return List.of();
    }
    String sql = """
        select id
        from permissions
        where code = any(?)
        """;
    return jdbc.query(sql, ps -> ps.setArray(1, ps.getConnection().createArrayOf("text", permissionCodes.toArray())),
        (rs, rowNum) -> rs.getObject("id", UUID.class));
  }

  public record UserRecord(UUID id, UUID tenantId, String email, String fullName, boolean isActive) {}
  public record RoleRecord(UUID id, UUID tenantId, String code, String name, String description) {}
  public record PermissionRecord(String code, String name, String description) {}
  public record BranchRecord(UUID id, String code, String name) {}
}
