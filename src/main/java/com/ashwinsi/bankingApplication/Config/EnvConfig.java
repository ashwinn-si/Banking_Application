package com.ashwinsi.bankingApplication.Config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.security")
@Getter
@Setter
public class EnvConfig {
    private Integer salt;
    private Long jwtExpirationTime;
    private String jwtSecret;
}
