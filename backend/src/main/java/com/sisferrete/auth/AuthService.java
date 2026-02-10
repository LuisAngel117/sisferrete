package com.sisferrete.auth;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import com.sisferrete.platform.audit.AuditService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final JwtService jwtService;
  private final JwtProperties jwtProperties;
  private final PasswordEncoder passwordEncoder;
  private final TotpService totpService;
  private final AuditService auditService;

  public AuthService(
      UserRepository userRepository,
      RefreshTokenRepository refreshTokenRepository,
      JwtService jwtService,
      JwtProperties jwtProperties,
      PasswordEncoder passwordEncoder,
      TotpService totpService,
      AuditService auditService) {
    this.userRepository = userRepository;
    this.refreshTokenRepository = refreshTokenRepository;
    this.jwtService = jwtService;
    this.jwtProperties = jwtProperties;
    this.passwordEncoder = passwordEncoder;
    this.totpService = totpService;
    this.auditService = auditService;
  }

  public LoginResponse login(LoginRequest request) {
    UserAccount user = userRepository.findByEmail(request.email())
        .orElseThrow(() -> unauthorized("Credenciales inválidas"));

    if (!user.active()) {
      throw unauthorized("Usuario inactivo");
    }

    if (!passwordEncoder.matches(request.password(), user.passwordHash())) {
      throw unauthorized("Credenciales inválidas");
    }

    if (user.twoFactorEnabled()) {
      if (!totpService.isValid(user.twoFactorSecret(), request.totp())) {
        throw unauthorized("TOTP requerido o inválido");
      }
    }

    UserContext context = buildContext(user);
    String accessToken = jwtService.createAccessToken(context);
    String refreshToken = refreshTokenRepository.createToken(
        context.tenantId(),
        context.userId(),
        Duration.ofSeconds(jwtProperties.getRefreshTokenTtlSeconds())
    );

    auditService.recordAuthSuccess(context.tenantId(), context.userId(), context.email(), "AUTH_LOGIN_SUCCESS");

    return new LoginResponse(accessToken, refreshToken, jwtService.getAccessTokenTtlSeconds());
  }

  public RefreshResponse refresh(RefreshRequest request) {
    RefreshTokenRecord token = refreshTokenRepository.findValid(request.refreshToken())
        .orElseThrow(() -> unauthorized("Refresh token inválido"));

    UserAccount user = userRepository.findById(token.userId())
        .orElseThrow(() -> unauthorized("Usuario no encontrado"));

    if (!user.active()) {
      throw unauthorized("Usuario inactivo");
    }

    UserContext context = buildContext(user);
    String accessToken = jwtService.createAccessToken(context);
    auditService.recordAuthSuccess(context.tenantId(), context.userId(), context.email(), "AUTH_REFRESH_SUCCESS");
    return new RefreshResponse(accessToken, jwtService.getAccessTokenTtlSeconds());
  }

  private UserContext buildContext(UserAccount user) {
    List<String> roles = userRepository.findRoles(user.id());
    List<String> permissions = userRepository.findPermissions(user.id());
    List<UUID> branchAccess = userRepository.findBranchAccess(user.id());

    return new UserContext(
        user.id(),
        user.tenantId(),
        user.email(),
        user.fullName(),
        roles,
        permissions,
        branchAccess
    );
  }

  private ResponseStatusException unauthorized(String message) {
    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
  }
}
