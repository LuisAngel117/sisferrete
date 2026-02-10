package com.sisferrete.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import com.sisferrete.platform.audit.AuditService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.ImmutableSecret;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http, AuditService auditService) throws Exception {
    http
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**", "/api/ping", "/actuator/health", "/error").permitAll()
            .anyRequest().authenticated()
        )
        .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler(auditService)))
        .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())));

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtEncoder jwtEncoder(JwtProperties properties) {
    SecretKey key = secretKey(properties);
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  public JwtDecoder jwtDecoder(JwtProperties properties) {
    SecretKey key = secretKey(properties);
    return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
  }

  private JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
      Collection<GrantedAuthority> authorities = new ArrayList<>();
      List<String> roles = jwt.getClaimAsStringList("roles");
      if (roles != null) {
        roles.forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role)));
      }
      List<String> permissions = jwt.getClaimAsStringList("permissions");
      if (permissions != null) {
        permissions.forEach(permission -> authorities.add(new SimpleGrantedAuthority("PERM_" + permission)));
      }
      return authorities;
    });
    return converter;
  }

  private AccessDeniedHandler accessDeniedHandler(AuditService auditService) {
    return (request, response, ex) -> {
      auditService.recordWithContext(
          "AUTH_PERMISSION_DENIED",
          "Acceso denegado",
          null,
          null,
          null,
          null,
          Map.of(
              "path", request.getRequestURI(),
              "method", request.getMethod()
          )
      );
      response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
    };
  }

  private SecretKey secretKey(JwtProperties properties) {
    byte[] raw = properties.getSecret().getBytes(StandardCharsets.UTF_8);
    if (raw.length < 32) {
      raw = sha256(raw);
    }
    return new SecretKeySpec(raw, "HmacSHA256");
  }

  private byte[] sha256(byte[] input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      return digest.digest(input);
    } catch (Exception ex) {
      throw new IllegalStateException("No se pudo inicializar SHA-256 para la clave JWT", ex);
    }
  }
}
