package io.github.ale.tripscheduler.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class PageController {

    @GetMapping({"", "/"})
    public String index(Authentication authentication) {
        if(isLogged(authentication))
            return "home";

        return "index";
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if(isLogged(authentication))
            return "home";

        return "login";
    }

    @GetMapping("/register")
    public String register(Authentication authentication) {
        if(isLogged(authentication))
            return "home";

        return "register";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        model.addAttribute("isLogged", isLogged(authentication));

        return "dashboard";
    }

    private boolean isLogged(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !authentication.getName().equals("anonymousUser");
    }
}