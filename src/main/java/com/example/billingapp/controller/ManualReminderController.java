package com.example.billingapp.controller;

import com.example.billingapp.service.ReminderService;
// import jakarta.mail.MessagingException; // <-- Hapus import ini
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manual-trigger")
public class ManualReminderController {

    private final ReminderService reminderService;

    public ManualReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping("/send-reminders")
    public ResponseEntity<String> triggerWeeklyReminder() {
        System.out.println("\n>>> FUNGSI MANUAL REMINDER DIPANGGIL <<<\n");
        reminderService.sendWeeklyReminders();
        
        return ResponseEntity.ok("✅ Reminder mingguan berhasil dipicu.");
    }
}