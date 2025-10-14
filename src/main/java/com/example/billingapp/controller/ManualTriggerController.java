package com.example.billingapp.controller;

import com.example.billingapp.service.ReminderService;
import com.example.billingapp.service.ReportService;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.YearMonth;

@RestController
@RequestMapping("/manual-trigger")
public class ManualTriggerController {

    private final ReportService reportService;
    private final ReminderService reminderService;

    public ManualTriggerController(ReportService reportService, ReminderService reminderService) {
        this.reportService = reportService;
        this.reminderService = reminderService;
    }

    @GetMapping("/send-report")
    public ResponseEntity<String> triggerMonthlyReport() {
       //YearMonth lastMonth = YearMonth.now().minusMonths(1);
        YearMonth currentMonth = YearMonth.now(); //.minusMonths(1);
        try {
            // Gunakan bulan dan tahun saat ini untuk testing cepat saja (jika production lastMonth.getMonthValue(), lastMonth.getYear())
            reportService.sendMonthlyReport(currentMonth.getMonthValue(), currentMonth.getYear());
            return ResponseEntity.ok("✅ Laporan bulanan untuk bulan " + currentMonth.getMonth() + " berhasil dipicu.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Gagal mengirim laporan: " + e.getMessage());
        }
    }

    @GetMapping("/send-reminders")
    public ResponseEntity<String> triggerWeeklyReminder() {
        try {
            reminderService.sendWeeklyReminders();
            return ResponseEntity.ok("✅ Reminder mingguan berhasil dipicu.");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("❌ Gagal mengirim reminder: " + e.getMessage());
        }
    }
}