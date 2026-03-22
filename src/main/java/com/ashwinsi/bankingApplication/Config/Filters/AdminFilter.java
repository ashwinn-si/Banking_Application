package com.ashwinsi.bankingApplication.Config.Filters;


import com.ashwinsi.bankingApplication.DTO.JwtDTO;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class AdminFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        JwtDTO jwtDTO = (JwtDTO) SecurityContextHolder.getContext().getAuthentication().getPrincipal();


        //TODO need to check if there is a admin with the userId in the database

        filterChain.doFilter(request, response);
    }
}
