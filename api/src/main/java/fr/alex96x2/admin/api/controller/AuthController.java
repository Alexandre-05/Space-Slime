package fr.alex96x2.admin.api.controller;

import fr.alex96x2.admin.api.dto.Dtos;
import fr.alex96x2.admin.api.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public Dtos.LoginResponse login(@RequestBody Dtos.LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me() {
        return ResponseEntity.ok().build();
    }
}
