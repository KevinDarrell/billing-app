package com.example.billingapp.controller;

import com.example.billingapp.model.User;
import com.example.billingapp.service.UserService;
import com.example.billingapp.repository.AreaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final AreaRepository areaRepository;


    public AuthController(UserService userService, AreaRepository areaRepository) {
        this.userService = userService;
        this.areaRepository = areaRepository; // 
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("allAreas", areaRepository.findAll());
        
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            String role = user.getRole();
            user.setRole(role);
            if ("USER".equals(user.getRole()) && user.getArea() == null) {
                redirectAttributes.addFlashAttribute("error_message", "Untuk Role 'User', Area Penugasan wajib diisi.");
                return "redirect:/register";
            }
            if ("ADMIN".equals(user.getRole())) {
                user.setArea(null);
            }

            userService.saveUser(user);
            
            redirectAttributes.addFlashAttribute("success_message", "Akun untuk '" + user.getUsername() + "' berhasil dibuat!");
            return "redirect:/"; 

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute("error_message", "Gagal membuat akun: " + e.getMessage());
            return "redirect:/register";
        }
    }
}