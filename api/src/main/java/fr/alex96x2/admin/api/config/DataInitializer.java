package fr.alex96x2.admin.api.config;

import fr.alex96x2.admin.api.entity.StaffAccountEntity;
import fr.alex96x2.admin.api.repository.StaffAccountRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedAdmin(StaffAccountRepository repository, PasswordEncoder encoder) {
        return args -> {
            if (repository.count() == 0) {
                StaffAccountEntity admin = new StaffAccountEntity();
                admin.setUsername("admin");
                admin.setPasswordHash(encoder.encode("admin123"));
                admin.setRole(StaffAccountEntity.StaffRole.FONDATEUR);
                admin.setCreatedAt(Instant.now());
                repository.save(admin);
            }
        };
    }
}
