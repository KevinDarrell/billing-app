package com.example.billingapp.controller;

import com.example.billingapp.model.User;
import com.example.billingapp.service.UserService;
import com.example.billingapp.repository.AreaRepository; // 3. Import AreaRepository
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // 2. Import untuk pesan notifikasi

@Controller
public class AuthController {

    private final UserService userService;
    private final AreaRepository areaRepository; // 4. Tambahkan AreaRepository

    // Best Practice: Menggunakan constructor injection
    public AuthController(UserService userService, AreaRepository areaRepository) {
        this.userService = userService;
        this.areaRepository = areaRepository; // 5. Inisialisasi di constructor
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        
        // ✅ PERBAIKAN: Kirim daftar semua area ke halaman HTML untuk mengisi dropdown
        model.addAttribute("allAreas", areaRepository.findAll());
        
        return "register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        try {
            // Logika untuk role: Form mengirim "USER" atau "ADMIN".
            // Kita tambahkan prefix "ROLE_" sebelum menyimpan.
            String role = user.getRole();
            user.setRole(role);

            // Validasi: Jika role adalah USER, Area tidak boleh kosong.
            if ("USER".equals(user.getRole()) && user.getArea() == null) {
                redirectAttributes.addFlashAttribute("error_message", "Untuk Role 'User', Area Penugasan wajib diisi.");
                return "redirect:/register";
            }
            
            // Jika role adalah ADMIN, pastikan areanya null (tidak terikat area manapun).
            if ("ADMIN".equals(user.getRole())) {
                user.setArea(null);
            }

            userService.saveUser(user); // Service akan menangani encoding password
            
            redirectAttributes.addFlashAttribute("success_message", "Akun untuk '" + user.getUsername() + "' berhasil dibuat!");
            // Redirect kembali ke halaman dashboard atau list user setelah sukses
            return "redirect:/"; 

        } catch (Exception e) {
            // Menangkap error jika username/email sudah ada
            redirectAttributes.addFlashAttribute("error_message", "Gagal membuat akun: " + e.getMessage());
            return "redirect:/register";
        }
    }
}