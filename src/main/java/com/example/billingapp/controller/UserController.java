package com.example.billingapp.controller;

import com.example.billingapp.model.User;
import com.example.billingapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/users")
@PreAuthorize("hasRole('ADMIN')") // Mengamankan seluruh controller ini hanya untuk Admin
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listUsers(Model model, HttpServletRequest request) {
        model.addAttribute("allUsers", userRepository.findAll());
        model.addAttribute("pageTitle", "Manajemen User");
        model.addAttribute("requestURI", request.getRequestURI());
        return "user_management";
    }

    @PostMapping("/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User tidak ditemukan"));
        
        // Membalikkan status: jika true menjadi false, jika false menjadi true
        user.setEnabled(!user.isEnabled()); 
        userRepository.save(user);
        
        String status = user.isEnabled() ? "diaktifkan" : "dinonaktifkan";
        redirectAttributes.addFlashAttribute("successMessage", "Akun '" + user.getUsername() + "' berhasil " + status + ".");
        return "redirect:/users";
    }
}