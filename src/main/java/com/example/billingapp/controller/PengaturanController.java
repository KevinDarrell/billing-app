package com.example.billingapp.controller;

import com.example.billingapp.model.Area;
import com.example.billingapp.model.User;
import com.example.billingapp.repository.UserRepository;
import com.example.billingapp.service.PengaturanService;
import com.example.billingapp.repository.AreaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/pengaturan")
public class PengaturanController {

    private final PengaturanService pengaturanService;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;

    public PengaturanController(PengaturanService pengaturanService, UserRepository userRepository, AreaRepository areaRepository) {
        this.pengaturanService = pengaturanService;
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
    }

    @GetMapping
    public String viewPengaturanPage(Model model, HttpServletRequest request) {
        model.addAttribute("emailSettings", pengaturanService.getEmailSettings());
        model.addAttribute("allUsers", userRepository.findAll());
        model.addAttribute("pageTitle", "Pengaturan Akun");
        model.addAttribute("requestURI", request.getRequestURI());
        return "pengaturan";
    }

    // Method untuk menyimpan pengaturan email
     @PostMapping("/email/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveEmailSettings(@RequestParam String emailTo, @RequestParam String emailCc, RedirectAttributes redirectAttributes) {
        pengaturanService.updateEmailSettings(emailTo, emailCc);
        redirectAttributes.addFlashAttribute("successMessage", "Pengaturan email berhasil diperbarui.");
        return "redirect:/pengaturan";
    }
     // ✅ METHOD UNTUK USER MENGUBAH PASSWORD SENDIRI
    @PostMapping("/password/user")
    public String changeOwnPassword(@RequestParam String currentPassword,
                                    @RequestParam String newPassword,
                                    @RequestParam String confirmPassword,
                                    Principal principal,
                                    RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password baru dan konfirmasi tidak cocok.");
            return "redirect:/pengaturan";
        }
        try {
            pengaturanService.changeOwnPassword(principal.getName(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password Anda berhasil diubah.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/pengaturan";
    }

    // ✅ METHOD UNTUK ADMIN MERESET PASSWORD USER LAIN
    @PostMapping("/password/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String changeUserPasswordByAdmin(@RequestParam Long userId,
                                            @RequestParam String newPassword,
                                            @RequestParam String confirmPassword,
                                            RedirectAttributes redirectAttributes) {
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Password baru dan konfirmasi tidak cocok.");
            return "redirect:/pengaturan";
        }
        try {
            pengaturanService.changePasswordByAdmin(userId, newPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Password user berhasil direset.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/pengaturan";
    }


    // ✅ PERBAIKAN 1: Method untuk MENAMPILKAN halaman manajemen Kepala Area
    @GetMapping("/kepala-area")
@PreAuthorize("hasRole('ADMIN')")
public String showKepalaAreaForm(Model model, HttpServletRequest request) {
    
    // --- LANGKAH DEBUGGING ---
    System.out.println("\n--- MEMULAI PROSES DEBUGGING 'showKepalaAreaForm' ---");
    List<User> allUsers = userRepository.findAll();
    System.out.println("1. Total user yang ditemukan di database: " + allUsers.size());
    
    for (User u : allUsers) {
        System.out.println("   - User: " + u.getUsername() + " | Role: '" + u.getRole() + "' | Area: " + u.getArea());
    }

    // Ini adalah filter yang sedang kita uji
    Map<Area, List<User>> usersByArea = allUsers.stream()
            .filter(user -> user.getArea() != null && "USER".equals(user.getRole()))
            .collect(Collectors.groupingBy(User::getArea));
    
    System.out.println("\n2. Hasil setelah filter (user.getArea() != null && \"USER\".equals(user.getRole())):");
    if (usersByArea.isEmpty()) {
        System.out.println("   -> TIDAK ADA USER YANG LOLOS FILTER.");
    } else {
        usersByArea.forEach((area, userList) -> {
            System.out.println("   -> Area " + area.getName() + ": " + userList.stream().map(User::getUsername).collect(Collectors.toList()));
        });
    }
    System.out.println("---------------------------------------------------\n");
    // --- AKHIR LANGKAH DEBUGGING ---

    Map<Area, User> currentHeads = new HashMap<>();
    for (Area area : areaRepository.findAll()) {
        userRepository.findByAreaAndIsAreaHead(area, true)
                .ifPresent(head -> currentHeads.put(area, head));
    }

    model.addAttribute("usersByArea", usersByArea);
    model.addAttribute("currentHeads", currentHeads);
    model.addAttribute("allAreas", areaRepository.findAll());
    model.addAttribute("pageTitle", "Manajemen Kepala Area");
    model.addAttribute("requestURI", request.getRequestURI());
    return "pengaturan_kepala_area";
}
    

    // ✅ PERBAIKAN 2: Method untuk MENYIMPAN perubahan Kepala Area
    @PostMapping("/kepala-area/save")
    @PreAuthorize("hasRole('ADMIN')")
    public String saveKepalaArea(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        // 'params' akan berisi semua data dari form, contoh: {"head_DKI": "12", "head_JABAR": "15"}
        pengaturanService.updateAreaHeads(params);
        redirectAttributes.addFlashAttribute("successMessage", "Perubahan Kepala Area berhasil disimpan.");
        return "redirect:/pengaturan/kepala-area";
    }
}