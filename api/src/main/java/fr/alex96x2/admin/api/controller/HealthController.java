package fr.alex96x2.admin.api.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Value("${app.build.id:local}")
    private String buildId;

    @GetMapping
    public Map<String, String> health() {
        return Map.of(
                "status", "ok",
                "build", buildId,
                "dashboard", "/dashboard/"
        );
    }
}
