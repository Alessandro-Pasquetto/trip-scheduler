package io.github.ale.tripscheduler.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class PageController {

    @GetMapping
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/home")
    public String home() {
        return "home";
    }

    @GetMapping("/plan-editor")
    public String planEditor() {
        return "plan-editor";
    }

    @GetMapping("/plan-editor/{idPlan}")
    public String planEditor(@PathVariable Long idPlan,
                             Model model) {

        model.addAttribute("idPlan", idPlan);
        return "plan-editor";
    }
}