package com.sisferrete.auth;

import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DemoUserSeeder implements ApplicationRunner {
  private static final Logger log = LoggerFactory.getLogger(DemoUserSeeder.class);
  private static final String TENANT_CODE = "FERRETERIA";
  private static final String BRANCH_CODE = "MATRIZ";
  private static final String ROLE_SUPERADMIN = "SUPERADMIN";
  private static final String ROLE_ADMIN = "ADMIN";

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final Environment environment;

  public DemoUserSeeder(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      Environment environment
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.environment = environment;
  }

  @Override
  public void run(ApplicationArguments args) {
    String email = environment.getProperty("SISFERRETE_ADMIN_EMAIL");
    String password = environment.getProperty("SISFERRETE_ADMIN_PASSWORD");
    String fullName = environment.getProperty("SISFERRETE_ADMIN_FULL_NAME", "Admin");

    if (email == null || email.isBlank() || password == null || password.isBlank()) {
      log.info("Seed admin omitido. Define SISFERRETE_ADMIN_EMAIL y SISFERRETE_ADMIN_PASSWORD para crear un usuario.");
      return;
    }

    if (userRepository.findByEmail(email).isPresent()) {
      log.info("Seed admin ya existe: {}", email);
      return;
    }

    Optional<UUID> tenantId = userRepository.findTenantIdByCode(TENANT_CODE);
    if (tenantId.isEmpty()) {
      log.warn("Seed admin omitido: tenant {} no encontrado.", TENANT_CODE);
      return;
    }

    Optional<UUID> branchId = userRepository.findBranchIdByCode(tenantId.get(), BRANCH_CODE);
    if (branchId.isEmpty()) {
      log.warn("Seed admin omitido: branch {} no encontrado.", BRANCH_CODE);
      return;
    }

    UUID roleId = userRepository.findRoleIdByCode(tenantId.get(), ROLE_SUPERADMIN)
        .or(() -> userRepository.findRoleIdByCode(tenantId.get(), ROLE_ADMIN))
        .orElse(null);

    if (roleId == null) {
      log.warn("Seed admin omitido: roles {} / {} no encontrados.", ROLE_SUPERADMIN, ROLE_ADMIN);
      return;
    }

    UUID userId = UUID.randomUUID();
    String passwordHash = passwordEncoder.encode(password);
    userRepository.insertUser(userId, tenantId.get(), email, fullName, passwordHash);
    userRepository.insertUserRole(tenantId.get(), userId, roleId);
    userRepository.insertUserBranchAccess(tenantId.get(), userId, branchId.get());

    log.info("Seed admin creado para {} (tenant={}, branch={}).", email, TENANT_CODE, BRANCH_CODE);
  }
}
