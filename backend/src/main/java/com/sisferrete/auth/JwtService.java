package com.sisferrete.auth;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;

  public JwtService(JwtEncoder jwtEncoder, JwtProperties jwtProperties) {
    this.jwtEncoder = jwtEncoder;
    this.jwtProperties = jwtProperties;
  }

  public String createAccessToken(UserContext context) {
    Instant now = Instant.now();
    Instant expiresAt = now.plusSeconds(jwtProperties.getAccessTokenTtlSeconds());

    JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer("sisferrete")
        .issuedAt(now)
        .expiresAt(expiresAt)
        .subject(context.userId().toString())
        .claim("user_id", context.userId().toString())
        .claim("tenant_id", context.tenantId().toString())
        .claim("email", context.email())
        .claim("full_name", context.fullName())
        .claim("roles", List.copyOf(context.roles()))
        .claim("permissions", List.copyOf(context.permissions()))
        .claim("branch_access", context.branchAccess().stream().map(UUID::toString).toList())
        .build();

    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }

  public long getAccessTokenTtlSeconds() {
    return jwtProperties.getAccessTokenTtlSeconds();
  }
}