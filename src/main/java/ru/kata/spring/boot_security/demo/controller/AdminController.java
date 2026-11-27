package ru.kata.spring.boot_security.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.service.UserService;
import java.util.Set;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService users;
    private final RoleRepository roles;
    public AdminController(UserService users, RoleRepository roles) {
        this.users = users;
        this.roles = roles;
    }
    @GetMapping
    public String list(Model model, @org.springframework.security.core.annotation.AuthenticationPrincipal User currentUser) {
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("roles", roles.findAll());
        model.addAttribute("users", users.findAll());
        return "admin";
    }
    @GetMapping("/new")
    public String add() {
        return "redirect:/admin#new-user";
    }
    @PostMapping("/create")
    public String create(@ModelAttribute User user, @RequestParam Set<Long> roleIds) {
        users.create(user, roleIds);
        return "redirect:/admin";
    }
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id) {
        return "redirect:/admin#edit-" + id;
    }
    @PostMapping("/{id}/update")
    public String update(@PathVariable Long id, @ModelAttribute User user, @RequestParam Set<Long> roleIds) {
        users.update(id, user, roleIds);
        return "redirect:/admin";
    }
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        users.delete(id);
        return "redirect:/admin";
    }
}
