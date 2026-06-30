package fr.alex96x2.admin.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Ip ip, Cors cors) {
    public record Jwt(String secret, int expirationHours) {}
    public record Ip(String hashSalt, String encryptionKey) {}
    public record Cors(String allowedOrigins) {}
}
