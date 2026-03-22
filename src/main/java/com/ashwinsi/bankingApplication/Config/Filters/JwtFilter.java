package com.ashwinsi.bankingApplication.Config.Filters;

import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import com.ashwinsi.bankingApplication.Utils.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {

  private JwtService jwtService;

  JwtFilter(JwtService jwtService) {
    this.jwtService = jwtService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return path.startsWith("/auth/") || path.startsWith("/api/auth/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String authorizationHeader = request.getHeader("authorization");

    if (authorizationHeader == null || !authorizationHeader.contains("Bearer")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = authorizationHeader.replace("Bearer ", "").trim();
    if (token.isBlank()) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    Boolean isValidToken = jwtService.isValidToken(token);

    if (!isValidToken) {
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      return;
    }

    JwtDTO jwtDTO = jwtService.parseJWTToken(token);
    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
        jwtDTO,
        null,
        List.of());
    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

    filterChain.doFilter(request, response);
  }
}
