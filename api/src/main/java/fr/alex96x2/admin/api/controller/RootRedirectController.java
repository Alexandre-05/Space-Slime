package fr.alex96x2.admin.api.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

@Controller
public class RootRedirectController {

    @GetMapping("/")
    public RedirectView root() {
        return new RedirectView("/dashboard/");
    }
}
