package com.sisferrete.platform.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestMetadataFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain
  ) throws ServletException, IOException {
    RequestMetadataHolder.set(new RequestMetadata(
        request.getRemoteAddr(),
        request.getHeader("User-Agent"),
        request.getHeader("X-Branch-Id"),
        request.getRequestURI(),
        request.getMethod()
    ));
    try {
      filterChain.doFilter(request, response);
    } finally {
      RequestMetadataHolder.clear();
    }
  }
}
