package com.example.billingapp.controller;

import com.example.billingapp.model.Area;
import com.example.billingapp.model.Pembayaran;
import com.example.billingapp.model.Tagihan;
import com.example.billingapp.model.User;
import com.example.billingapp.repository.PembayaranRepository;
import com.example.billingapp.repository.TagihanRepository;
import com.example.billingapp.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/pembayaran")
public class PembayaranController {

    private final PembayaranRepository pembayaranRepository;
    private final TagihanRepository tagihanRepository;
    private final UserRepository userRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public PembayaranController(PembayaranRepository pembayaranRepository, TagihanRepository tagihanRepository, UserRepository userRepository) {
        this.pembayaranRepository = pembayaranRepository;
        this.tagihanRepository = tagihanRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String listPembayaran(Model model, Principal principal, HttpServletRequest request) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            model.addAttribute("pembayarans", pembayaranRepository.findAll());
        } else {
            model.addAttribute("pembayarans", pembayaranRepository.findAllByArea(currentUser.getArea()));
        }
        
        model.addAttribute("pageTitle", "Daftar Pembayaran");
        model.addAttribute("requestURI", request.getRequestURI());
        return "pembayaran_list";
    }

    @GetMapping("/add")
    public String addForm(Model model, Principal principal, HttpServletRequest request) {
        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        List<Tagihan> tagihanOptions;
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            tagihanOptions = tagihanRepository.findByStatus("Belum Dibayar");
        } else {
            tagihanOptions = tagihanRepository.findUnpaidByArea(currentUser.getArea());
        }

        model.addAttribute("pembayaran", new Pembayaran());
        model.addAttribute("tagihans", tagihanOptions);
        model.addAttribute("pageTitle", "Tambah Pembayaran");
        model.addAttribute("requestURI", request.getRequestURI());
        return "pembayaran_add";
    }

    @PostMapping("/save")
    public String savePembayaran(@ModelAttribute Pembayaran pembayaran,
                                 @RequestParam("file") MultipartFile file,
                                 Principal principal,
                                 RedirectAttributes redirectAttributes) throws IOException {

        User currentUser = userRepository.findByUsername(principal.getName()).orElseThrow();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Long tagihanId = pembayaran.getTagihan().getId();
        Tagihan tagihan;

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            tagihan = tagihanRepository.findById(tagihanId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tagihan tidak ditemukan"));
        } else {
            tagihan = tagihanRepository.findByIdAndLokasiArea(tagihanId, currentUser.getArea())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Anda tidak memiliki akses ke tagihan ini"));
        }

        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bukti transfer wajib diunggah.");
            return "redirect:/pembayaran/add";
        }

        File directory = new File(uploadDir);
        if (!directory.exists()) directory.mkdirs();
        
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        file.transferTo(new File(directory.getAbsolutePath() + File.separator + fileName));

        pembayaran.setTagihan(tagihan);
        pembayaran.setBuktiTransferPath(fileName);
        pembayaranRepository.save(pembayaran);

        tagihan.setStatus("Sudah Dibayar");
        tagihan.setNote(null);

        tagihanRepository.save(tagihan);
        
        redirectAttributes.addFlashAttribute("successMessage", "Pembayaran berhasil disimpan.");
        return "redirect:/pembayaran";
    }

    @GetMapping("/download/{id}")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) throws MalformedURLException {
        Pembayaran pembayaran = pembayaranRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Pembayaran tidak ditemukan"));

        if (pembayaran.getBuktiTransferPath() == null) {
            return ResponseEntity.notFound().build();
        }

        Path filePath = Paths.get(uploadDir, pembayaran.getBuktiTransferPath());
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            return ResponseEntity.notFound().build(); }
        }

        // Helper method: format Double ke Rupiah
    private String formatRupiah(Double nilai) {
        if (nilai == null) return "-";
        return "Rp " + String.format("%,.0f", nilai).replace(",", ".");

    }
}