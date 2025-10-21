package com.example.billingapp.controller;

import com.example.billingapp.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;

@RestController
@RequestMapping("/manual-trigger")
public class ManualReportController {

    private final ReportService reportService;

    public ManualReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/send-report")
    public ResponseEntity<String> triggerMonthlyReport() {
        System.out.println("\n>>> FUNGSI MANUAL REPORT DIPANGGIL <<<\n");
        YearMonth currentMonth = YearMonth.now();
        
        reportService.sendMonthlyReport(currentMonth.getMonthValue(), currentMonth.getYear());
        
        return ResponseEntity.ok("✅ Laporan bulanan untuk bulan " + currentMonth.getMonth() + " berhasil dipicu.");
    }
}