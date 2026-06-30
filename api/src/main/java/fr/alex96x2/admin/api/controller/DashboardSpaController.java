package fr.alex96x2.admin.api.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardSpaController {

    private static final Resource INDEX = new ClassPathResource("static/index.html");

    @GetMapping("/dashboard")
    public String redirectDashboard() {
        return "redirect:/dashboard/";
    }

    @GetMapping(value = "/dashboard/", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<Resource> dashboardRoot() {
        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(INDEX);
    }
}
