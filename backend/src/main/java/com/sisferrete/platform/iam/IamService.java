package com.sisferrete.platform.iam;

import com.sisferrete.platform.audit.AuditService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class IamService {
  private final IamRepository repository;
  private final PasswordEncoder passwordEncoder;
  private final AuditService auditService;

  public IamService(IamRepository repository, PasswordEncoder passwordEncoder, AuditService auditService) {
    this.repository = repository;
    this.passwordEncoder = passwordEncoder;
    this.auditService = auditService;
  }

  public List<IamUserResponse> listUsers(UUID tenantId) {
    return repository.listUsers(tenantId).stream()
        .map(user -> new IamUserResponse(
            user.id(),
            user.email(),
            user.fullName(),
            user.isActive(),
            repository.findUserRoles(user.id()),
            repository.findUserBranches(user.id())
        ))
        .toList();
  }

  public IamUserResponse getUser(UUID tenantId, UUID userId) {
    IamRepository.UserRecord user = repository.findUser(tenantId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    return new IamUserResponse(
        user.id(),
        user.email(),
        user.fullName(),
        user.isActive(),
        repository.findUserRoles(user.id()),
        repository.findUserBranches(user.id())
    );
  }

  @Transactional
  public IamUserResponse createUser(UUID tenantId, CreateUserRequest request) {
    if (repository.findUserByEmail(tenantId, request.email()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Email ya registrado");
    }
    String passwordHash = passwordEncoder.encode(request.password());
    UUID userId = repository.insertUser(
        tenantId,
        request.email(),
        request.fullName(),
        passwordHash,
        request.isActive()
    );
    auditService.recordWithContext(
        "IAM_USER_CREATED",
        "Usuario creado",
        "User",
        userId,
        null,
        Map.of(
            "email", request.email(),
            "fullName", request.fullName(),
            "isActive", request.isActive()
        ),
        null
    );
    return getUser(tenantId, userId);
  }

  @Transactional
  public IamUserResponse updateUser(UUID tenantId, UUID userId, UpdateUserRequest request) {
    IamRepository.UserRecord before = repository.findUser(tenantId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    int updated = repository.updateUser(tenantId, userId, request.fullName(), request.isActive());
    if (updated == 0) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
    }
    auditService.recordWithContext(
        "IAM_USER_UPDATED",
        "Usuario actualizado",
        "User",
        userId,
        Map.of(
            "fullName", before.fullName(),
            "isActive", before.isActive()
        ),
        Map.of(
            "fullName", request.fullName(),
            "isActive", request.isActive()
        ),
        null
    );
    return getUser(tenantId, userId);
  }

  @Transactional
  public IamUserResponse replaceUserRoles(UUID tenantId, UUID userId, ReplaceUserRolesRequest request) {
    repository.findUser(tenantId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    List<String> roleCodes = request.roleCodes();
    List<UUID> roleIds = repository.findRoleIdsByCodes(tenantId, roleCodes);
    if (roleIds.size() != roleCodes.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Roles inválidos");
    }
    List<String> before = repository.findUserRoles(userId);
    repository.replaceUserRoles(tenantId, userId, roleIds);
    auditService.recordWithContext(
        "IAM_USER_ROLES_CHANGED",
        "Roles de usuario actualizados",
        "User",
        userId,
        Map.of("roles", before),
        Map.of("roles", roleCodes),
        null
    );
    return getUser(tenantId, userId);
  }

  @Transactional
  public IamUserResponse replaceUserBranches(UUID tenantId, UUID userId, ReplaceUserBranchesRequest request) {
    repository.findUser(tenantId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    List<UUID> branchIds = resolveBranches(tenantId, request);
    List<UUID> before = repository.findUserBranches(userId);
    repository.replaceUserBranches(tenantId, userId, branchIds);
    auditService.recordWithContext(
        "IAM_USER_BRANCHES_CHANGED",
        "Sucursales de usuario actualizadas",
        "User",
        userId,
        Map.of("branches", before),
        Map.of("branches", branchIds),
        null
    );
    return getUser(tenantId, userId);
  }

  public List<IamRoleResponse> listRoles(UUID tenantId) {
    return repository.listRoles(tenantId).stream()
        .map(role -> new IamRoleResponse(
            role.id(),
            role.code(),
            role.name(),
            role.description(),
            repository.findRolePermissions(role.id())
        ))
        .toList();
  }

  @Transactional
  public IamRoleResponse createRole(UUID tenantId, CreateRoleRequest request) {
    if (repository.findRoleByCode(tenantId, request.code()).isPresent()) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Código de rol ya existe");
    }
    UUID roleId = repository.insertRole(tenantId, request.code(), request.name(), request.description());
    auditService.recordWithContext(
        "IAM_ROLE_CREATED",
        "Rol creado",
        "Role",
        roleId,
        null,
        Map.of(
            "code", request.code(),
            "name", request.name()
        ),
        null
    );
    return listRoles(tenantId).stream().filter(role -> role.roleId().equals(roleId)).findFirst()
        .orElseGet(() -> new IamRoleResponse(roleId, request.code(), request.name(), request.description(), List.of()));
  }

  @Transactional
  public IamRoleResponse updateRole(UUID tenantId, UUID roleId, UpdateRoleRequest request) {
    IamRepository.RoleRecord before = repository.findRole(tenantId, roleId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    repository.updateRole(tenantId, roleId, request.name(), request.description());
    auditService.recordWithContext(
        "IAM_ROLE_UPDATED",
        "Rol actualizado",
        "Role",
        roleId,
        Map.of(
            "name", before.name(),
            "description", before.description()
        ),
        Map.of(
            "name", request.name(),
            "description", request.description()
        ),
        null
    );
    return listRoles(tenantId).stream().filter(role -> role.roleId().equals(roleId)).findFirst()
        .orElseGet(() -> new IamRoleResponse(roleId, before.code(), request.name(), request.description(), List.of()));
  }

  @Transactional
  public IamRoleResponse replaceRolePermissions(UUID tenantId, UUID roleId, ReplaceRolePermissionsRequest request) {
    IamRepository.RoleRecord role = repository.findRole(tenantId, roleId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Rol no encontrado"));
    List<String> codes = request.permissionCodes();
    List<UUID> permissionIds = repository.findPermissionIdsByCodes(codes);
    if (permissionIds.size() != codes.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Permisos inválidos");
    }
    List<String> before = repository.findRolePermissions(roleId);
    repository.replaceRolePermissions(roleId, permissionIds);
    auditService.recordWithContext(
        "IAM_ROLE_PERMISSIONS_CHANGED",
        "Permisos de rol actualizados",
        "Role",
        roleId,
        Map.of("permissions", before),
        Map.of("permissions", codes),
        null
    );
    return new IamRoleResponse(roleId, role.code(), role.name(), role.description(), codes);
  }

  public List<IamPermissionResponse> listPermissions() {
    return repository.listPermissions().stream()
        .map(permission -> new IamPermissionResponse(
            permission.code(),
            permission.name(),
            permission.description(),
            moduleFor(permission.code())
        ))
        .toList();
  }

  public List<IamBranchResponse> listBranches(UUID tenantId) {
    return repository.listBranches(tenantId).stream()
        .map(branch -> new IamBranchResponse(branch.id(), branch.code(), branch.name()))
        .toList();
  }

  private List<UUID> resolveBranches(UUID tenantId, ReplaceUserBranchesRequest request) {
    if (Boolean.TRUE.equals(request.allBranches())) {
      return repository.listBranchIds(tenantId);
    }
    List<UUID> branchIds = request.branchIds() == null ? List.of() : request.branchIds();
    List<UUID> valid = repository.findBranchIds(tenantId, branchIds);
    if (valid.size() != branchIds.size()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Sucursales inválidas");
    }
    return new ArrayList<>(valid);
  }

  private String moduleFor(String code) {
    if (code == null || code.isBlank()) {
      return "GENERAL";
    }
    if (code.contains("_")) {
      return code.substring(0, code.indexOf('_'));
    }
    if (code.contains(".")) {
      return code.substring(0, code.indexOf('.')).toUpperCase();
    }
    return "GENERAL";
  }

  public record CreateUserRequest(
      @NotBlank String email,
      @NotBlank String fullName,
      @NotBlank String password,
      boolean isActive
  ) {}

  public record UpdateUserRequest(
      @NotBlank String fullName,
      boolean isActive
  ) {}

  public record ReplaceUserRolesRequest(@NotEmpty List<String> roleCodes) {}
  public record ReplaceUserBranchesRequest(List<UUID> branchIds, Boolean allBranches) {}

  public record CreateRoleRequest(
      @NotBlank String code,
      @NotBlank String name,
      String description
  ) {}

  public record UpdateRoleRequest(
      @NotBlank String name,
      String description
  ) {}

  public record ReplaceRolePermissionsRequest(@NotEmpty List<String> permissionCodes) {}

  public record IamUserResponse(
      UUID userId,
      String email,
      String fullName,
      boolean isActive,
      List<String> roleCodes,
      List<UUID> branchIds
  ) {}

  public record IamRoleResponse(
      UUID roleId,
      String code,
      String name,
      String description,
      List<String> permissionCodes
  ) {}

  public record IamPermissionResponse(
      String code,
      String name,
      String description,
      String module
  ) {}

  public record IamBranchResponse(UUID branchId, String code, String name) {}
}
