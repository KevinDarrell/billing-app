package com.example.billingapp.controller;

import com.example.billingapp.model.Area;
import com.example.billingapp.model.User;
import com.example.billingapp.repository.CompanyRepository;
import com.example.billingapp.repository.LokasiRepository;
import com.example.billingapp.repository.TagihanRepository;
import com.example.billingapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Controller
public class DashboardController {

    private final CompanyRepository companyRepository;
    private final LokasiRepository lokasiRepository;
    private final TagihanRepository tagihanRepository;
    private final UserRepository userRepository;

    public DashboardController(CompanyRepository companyRepository,
                               LokasiRepository lokasiRepository,
                               TagihanRepository tagihanRepository,
                               UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.lokasiRepository = lokasiRepository;
        this.tagihanRepository = tagihanRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(value = "month", required = false) Integer month,
                            @RequestParam(value = "year", required = false) Integer year,
                            Model model, Principal principal, HttpServletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();

        if (month == null) month = LocalDate.now().getMonthValue();
        if (year == null) year = LocalDate.now().getYear();

        long jumlahVendor = companyRepository.count();
        long jumlahLokasi;
        Long totalTagihan;
        Long totalSudahBayar;
        
        // ✅ LOGIKA KUNCI: Hitung statistik berdasarkan role
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            // JIKA ADMIN: Hitung semua data
            jumlahLokasi = lokasiRepository.count();
            totalTagihan = tagihanRepository.countByMonthAndYear(month, year);
            totalSudahBayar = tagihanRepository.countByStatusAndMonthAndYear("Sudah Dibayar", month, year);
        } else {
            // JIKA USER BIASA: Hitung data berdasarkan areanya saja
            Area userArea = currentUser.getArea();
            jumlahLokasi = lokasiRepository.countByArea(userArea);
            totalTagihan = tagihanRepository.countByLokasiAreaAndMonthAndYear(userArea, month, year);
            totalSudahBayar = tagihanRepository.countByStatusAndLokasiAreaAndMonthAndYear("Sudah Dibayar", userArea, month, year);
        }

        model.addAttribute("jumlahVendor", jumlahVendor);
        model.addAttribute("jumlahLokasi", jumlahLokasi);
        model.addAttribute("totalTagihan", totalTagihan != null ? totalTagihan : 0);
        model.addAttribute("totalSudahBayar", totalSudahBayar != null ? totalSudahBayar : 0);
        
        model.addAttribute("selectedMonth", month);
        model.addAttribute("selectedYear", year);
        model.addAttribute("months", getMonthList());
        model.addAttribute("pageTitle", "Dashboard");
        model.addAttribute("requestURI", request.getRequestURI());

        return "dashboard";
    }

    private Map<Integer, String> getMonthList() {
        Map<Integer, String> months = new LinkedHashMap<>();
        for (int i = 1; i <= 12; i++) {
            months.put(i, Month.of(i).getDisplayName(TextStyle.FULL, new Locale("id", "ID")));
        }
        return months;
    }

    // Daftar tahun tidak lagi digunakan di form, bisa dihapus jika mau
    private Map<Integer, Integer> getYearList() {
        Map<Integer, Integer> years = new LinkedHashMap<>();
        int currentYear = LocalDate.now().getYear();
        for (int i = currentYear - 3; i <= currentYear + 1; i++) {
            years.put(i, i);
        }
        return years;
    }
}