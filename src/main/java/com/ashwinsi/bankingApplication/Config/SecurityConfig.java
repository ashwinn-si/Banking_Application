package com.ashwinsi.bankingApplication.Config;

import com.ashwinsi.bankingApplication.Config.Filters.AdminFilter;
import com.ashwinsi.bankingApplication.Config.Filters.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
  private JwtFilter jwtFilter;
  private AdminFilter adminFilter;
  private EnvConfig envConfig;

  SecurityConfig(JwtFilter jwtFilter, AdminFilter adminFilter, EnvConfig envConfig) {
    this.jwtFilter = jwtFilter;
    this.adminFilter = adminFilter;
    this.envConfig = envConfig;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    Integer SALT = envConfig.getSalt();
    return new BCryptPasswordEncoder(SALT);
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowCredentials(true);

    config.setAllowedHeaders(List.of("*"));

    config.setAllowedMethods(List.of("GET", "PUT", "DELETE", "POST"));

    // TODO need to add production env later
    config.setAllowedOrigins(List.of("http://localhost:5173"));

    config.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

    source.registerCorsConfiguration("/**", config);

    return source;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
        // needed to be disabled for jwt based login
        .csrf(c -> c.disable())
        .cors(c -> c.configurationSource(corsConfigurationSource()))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/login").permitAll()
            .requestMatchers("/signup").permitAll()
            .anyRequest().authenticated()

        )
        .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
        .addFilterAfter(adminFilter, JwtFilter.class);

    return http.build();
  }
}
