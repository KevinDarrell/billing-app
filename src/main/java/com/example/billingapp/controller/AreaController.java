package com.example.billingapp.controller;
import com.example.billingapp.model.Area;
import com.example.billingapp.repository.AreaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/areas")
@PreAuthorize("hasRole('ADMIN')")
public class AreaController {

    private final AreaRepository areaRepository;

    public AreaController(AreaRepository areaRepository) {
        this.areaRepository = areaRepository;
    }

    @GetMapping
    public String listAreas(Model model, HttpServletRequest request) {
        model.addAttribute("areas", areaRepository.findAll());
        model.addAttribute("pageTitle", "Manajemen Area");
        model.addAttribute("requestURI", request.getRequestURI());
        return "area_list"; 
    }
    

    @GetMapping("/add")
    public String addAreaForm(Model model, HttpServletRequest request) {
        model.addAttribute("area", new Area());
        model.addAttribute("pageTitle", "Tambah Area");
        model.addAttribute("requestURI", request.getRequestURI());
        return "area_form"; 
    }


    @PostMapping("/save")
    public String saveArea(@ModelAttribute Area area, RedirectAttributes redirectAttributes) {
        areaRepository.save(area);
        redirectAttributes.addFlashAttribute("successMessage", "Area berhasil disimpan.");
        return "redirect:/areas";
    }

  
    @GetMapping("/edit/{id}")
    public String editAreaForm(@PathVariable Long id, Model model, HttpServletRequest request) {
        Area area = areaRepository.findById(id).orElseThrow();
        model.addAttribute("area", area);
        model.addAttribute("pageTitle", "Edit Area");
        model.addAttribute("requestURI", request.getRequestURI());
        return "area_form";
    }


    @GetMapping("/delete/{id}")
    public String deleteArea(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        areaRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("successMessage", "Area berhasil dihapus.");
        return "redirect:/areas";
    }
}