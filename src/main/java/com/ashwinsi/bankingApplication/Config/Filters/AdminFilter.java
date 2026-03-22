package com.ashwinsi.bankingApplication.Config.Filters;

import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AdminFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return path.startsWith("/auth/") || path.startsWith("/api/auth/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (SecurityContextHolder.getContext().getAuthentication() == null
        || !(SecurityContextHolder.getContext().getAuthentication().getPrincipal() instanceof JwtDTO)) {
      filterChain.doFilter(request, response);
      return;
    }

    JwtDTO jwtDTO = (JwtDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    // TODO need to check if there is a admin with the userId in the database

    filterChain.doFilter(request, response);
  }
}
