package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kata.spring.boot_security.demo.model.User;
@Controller
public class HomeController {
    @GetMapping("/")
    public String index() {
        return "redirect:/login";
    }
    @GetMapping("/login")
    public String login() {
        return "login";
    }
    @GetMapping("/user")
    public String user(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("user", user);
        return "user";
    }
}
