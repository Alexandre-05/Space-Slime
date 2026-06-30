package fr.alex96x2.admin.api.service;

import fr.alex96x2.admin.api.dto.Dtos;
import fr.alex96x2.admin.api.entity.StaffAccountEntity;
import fr.alex96x2.admin.api.repository.StaffAccountRepository;
import fr.alex96x2.admin.api.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {

    private final StaffAccountRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(StaffAccountRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Dtos.LoginResponse login(Dtos.LoginRequest request) {
        StaffAccountEntity account = repository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Identifiants invalides"));
        if (!passwordEncoder.matches(request.password(), account.getPasswordHash())) {
            throw new IllegalArgumentException("Identifiants invalides");
        }
        account.setLastLogin(Instant.now());
        repository.save(account);
        String token = jwtService.generateToken(account.getUsername(), account.getRole().name());
        return new Dtos.LoginResponse(token, account.getUsername(), account.getRole().name());
    }
}
