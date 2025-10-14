package com.example.billingapp.controller;

import com.example.billingapp.model.Area;
import com.example.billingapp.model.Company;
import com.example.billingapp.model.Lokasi;
import com.example.billingapp.model.User;
import com.example.billingapp.repository.CompanyRepository;
import com.example.billingapp.repository.LokasiRepository;
import com.example.billingapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class MController {
    private final CompanyRepository companyRepository;
    private final LokasiRepository lokasiRepository;
    private final UserRepository userRepository; // ✅ Tambahkan UserRepository

    public MController(CompanyRepository companyRepository, LokasiRepository lokasiRepository, UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.lokasiRepository = lokasiRepository;
        this.userRepository = userRepository; // ✅ Tambahkan di constructor
    }

    @GetMapping("/datavendorlokasi")
    public String home(Model model, Principal principal, HttpServletRequest request) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // ✅ LOGIKA FILTER: Cek role, lalu filter data lokasi berdasarkan area
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("lokasis", lokasiRepository.findAll()); // Admin lihat semua
        } else {
            model.addAttribute("lokasis", lokasiRepository.findByArea(currentUser.getArea())); // User lihat area sendiri
        }
        
        model.addAttribute("vendors", companyRepository.findAll());
        model.addAttribute("pageTitle", "Data Master");
        model.addAttribute("requestURI", request.getRequestURI());
        return "data_vendor_lokasi";
    }

    @GetMapping("/addVendor")
    public String addVendorForm(Model model, HttpServletRequest request) {
        model.addAttribute("vendor", new Company());
        model.addAttribute("pageTitle", "Tambah Vendor");
        model.addAttribute("requestURI", request.getRequestURI());
        return "add_vendor";
    }
    
    @PostMapping("/saveVendor")
    public String saveVendor(@ModelAttribute Company vendor) {
        companyRepository.save(vendor);
        return "redirect:/datavendorlokasi";
    }

    @GetMapping("/addLokasi")
    public String addLokasiForm(Model model, HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("lokasi", new Lokasi());

        // ✅ LOGIKA FILTER: Admin bisa pilih semua area, User tidak
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("allAreas", Area.values());
        }

        model.addAttribute("pageTitle", "Tambah Lokasi");
        model.addAttribute("requestURI", request.getRequestURI());
        return "add_lokasi";
    }
    
    @PostMapping("/saveLokasi")
    public String saveLokasi(@ModelAttribute Lokasi lokasi, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        // ✅ LOGIKA KEAMANAN: Paksa 'area' sesuai area user jika bukan admin
        if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            lokasi.setArea(currentUser.getArea());
        }
        
        if (lokasi.getArea() == null) {
            redirectAttributes.addFlashAttribute("error_message", "Area wajib diisi.");
            return "redirect:/addLokasi";
        }

        lokasiRepository.save(lokasi);
        redirectAttributes.addFlashAttribute("success_message", "Lokasi berhasil disimpan.");
        return "redirect:/datavendorlokasi";
    }

    @GetMapping("/editVendor/{id}")
    public String editVendorForm(@PathVariable("id") Long id, Model model, HttpServletRequest request) {
        Company vendor = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendor tidak ditemukan: " + id));
        
        model.addAttribute("vendor", vendor);
        model.addAttribute("pageTitle", "Edit Vendor");
        model.addAttribute("requestURI", request.getRequestURI());
        return "edit_vendor";
    }

    @PostMapping("/updateVendor/{id}")
    public String updateVendor(@PathVariable("id") Long id, @ModelAttribute Company vendorDetails, RedirectAttributes redirectAttributes) {
        Company vendor = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vendor tidak ditemukan: " + id));
        
        vendor.setNamaVendor(vendorDetails.getNamaVendor());
        vendor.setEmailVendor(vendorDetails.getEmailVendor());
        companyRepository.save(vendor);
        
        redirectAttributes.addFlashAttribute("successMessage", "Data vendor berhasil diperbarui.");
        return "redirect:/datavendorlokasi";
    }

    // --- FITUR EDIT LOKASI ---

    @GetMapping("/editLokasi/{id}")
    public String editLokasiForm(@PathVariable("id") Long id, Model model, Principal principal, HttpServletRequest request) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Lokasi lokasi;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            lokasi = lokasiRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Lokasi tidak ditemukan"));
            model.addAttribute("allAreas", Area.values()); // Admin bisa ubah area
        } else {
            lokasi = lokasiRepository.findByIdAndArea(id, currentUser.getArea()) // Method baru di LokasiRepository
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lokasi tidak ditemukan atau Anda tidak punya akses"));
        }

        model.addAttribute("lokasi", lokasi);
        model.addAttribute("pageTitle", "Edit Lokasi");
        model.addAttribute("requestURI", request.getRequestURI());
        return "edit_lokasi";
    }

    @PostMapping("/updateLokasi/{id}")
    public String updateLokasi(@PathVariable("id") Long id, @ModelAttribute Lokasi lokasiDetails, Principal principal, RedirectAttributes redirectAttributes) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Lokasi lokasi;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            lokasi = lokasiRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Lokasi tidak ditemukan"));
        } else {
            lokasi = lokasiRepository.findByIdAndArea(id, currentUser.getArea())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Anda tidak punya akses"));
        }

        lokasi.setCompany(lokasiDetails.getCompany());
        lokasi.setNamaLokasi(lokasiDetails.getNamaLokasi());
        lokasi.setAlamat(lokasiDetails.getAlamat());
        lokasi.setKota(lokasiDetails.getKota());
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
           lokasi.setArea(lokasiDetails.getArea()); // Hanya admin yang bisa ubah area
        }

        lokasiRepository.save(lokasi);
        redirectAttributes.addFlashAttribute("successMessage", "Data lokasi berhasil diperbarui.");
        return "redirect:/datavendorlokasi";
    }
}