package com.example.billingapp.controller;

import com.example.billingapp.model.Lokasi;
import com.example.billingapp.model.Tagihan;
import com.example.billingapp.model.User;
import com.example.billingapp.repository.CompanyRepository;
import com.example.billingapp.repository.LokasiRepository;
import com.example.billingapp.repository.TagihanRepository;
import com.example.billingapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tagihan")
public class TagihanController {

    private final TagihanRepository tagihanRepository;
    private final CompanyRepository companyRepository;
    private final LokasiRepository lokasiRepository;
    private final UserRepository userRepository;

    public TagihanController(TagihanRepository tagihanRepository, CompanyRepository companyRepository,
                             LokasiRepository lokasiRepository, UserRepository userRepository) {
        this.tagihanRepository = tagihanRepository;
        this.companyRepository = companyRepository;
        this.lokasiRepository = lokasiRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listTagihan(Model model, Principal principal, HttpServletRequest request) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        List<Tagihan> tagihans;


        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            tagihans = tagihanRepository.findAll();
        } else {
            tagihans = tagihanRepository.findByLokasiArea(currentUser.getArea());
        }
        
        model.addAttribute("tagihans", tagihans);


        Map<Long, String> rupiahMap = new HashMap<>();
        for (Tagihan t : tagihans) {
            rupiahMap.put(t.getId(), formatRupiah(t.getNilaiPaymentVoucher()));
        }
        model.addAttribute("rupiahMap", rupiahMap);
        
        model.addAttribute("pageTitle", "Daftar Tagihan");
        model.addAttribute("requestURI", request.getRequestURI());
        return "tagihan_list";
    }

    @GetMapping("/add")
    public String addForm(Model model, Principal principal, HttpServletRequest request) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
    
        List<Lokasi> lokasiOptions;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            lokasiOptions = lokasiRepository.findAll();
        } else {
            lokasiOptions = lokasiRepository.findByArea(currentUser.getArea());
        }

        model.addAttribute("tagihan", new Tagihan());
        model.addAttribute("vendors", companyRepository.findAll());
        model.addAttribute("lokasis", lokasiOptions);
        model.addAttribute("pageTitle", "Tambah Tagihan");
        model.addAttribute("requestURI", request.getRequestURI());
        return "tagihan_add";
    }

    @PostMapping("/save")
    public String saveTagihan(@ModelAttribute Tagihan tagihan, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        tagihan.setCreatedBy(currentUser);
        if (tagihan.getStatus() == null || tagihan.getStatus().isEmpty()) {
            tagihan.setStatus("Belum Dibayar");
        }
        tagihanRepository.save(tagihan);
        return "redirect:/tagihan";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model, Principal principal, HttpServletRequest request) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    
        Tagihan tagihan;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            tagihan = tagihanRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tagihan tidak ditemukan"));
        } else {
            tagihan = tagihanRepository.findByIdAndLokasiArea(id, currentUser.getArea())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tagihan tidak ditemukan atau Anda tidak punya akses"));
        }

        model.addAttribute("tagihan", tagihan);
        model.addAttribute("vendors", companyRepository.findAll());
        model.addAttribute("lokasis", auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) ? lokasiRepository.findAll() : lokasiRepository.findByArea(currentUser.getArea()));
        
        model.addAttribute("pageTitle", "Edit Tagihan");
        model.addAttribute("requestURI", request.getRequestURI());
        return "tagihan_edit";
    }
    @PostMapping("/update/{id}")
    public String updateTagihan(@PathVariable Long id, @ModelAttribute Tagihan tagihanDetails, Principal principal) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Tagihan existingTagihan;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            existingTagihan = tagihanRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        } else {
            existingTagihan = tagihanRepository.findByIdAndLokasiArea(id, currentUser.getArea()).orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN));
        }
        
        existingTagihan.setVendor(tagihanDetails.getVendor());
        existingTagihan.setLokasi(tagihanDetails.getLokasi());
        existingTagihan.setTanggalDiterima(tagihanDetails.getTanggalDiterima());
        existingTagihan.setNilaiPaymentVoucher(tagihanDetails.getNilaiPaymentVoucher());
        existingTagihan.setNote(tagihanDetails.getNote());

        tagihanRepository.save(existingTagihan);
        return "redirect:/tagihan";
    }

    @GetMapping("/delete/{id}")
    public String deleteTagihan(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
         User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Tagihan tagihan;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            tagihan = tagihanRepository.findById(id).orElse(null);
        } else {
            tagihan = tagihanRepository.findByIdAndLokasiArea(id, currentUser.getArea()).orElse(null);
        }

        if (tagihan != null) {
            tagihanRepository.delete(tagihan);
            redirectAttributes.addFlashAttribute("successMessage", "Tagihan berhasil dihapus.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Tagihan tidak ditemukan atau Anda tidak punya akses.");
        }

        return "redirect:/tagihan";
    }

    private String formatRupiah(Double nilai) {
        if (nilai == null) return "-";
        return "Rp " + String.format("%,.0f", nilai).replace(",", ".");
    }
}