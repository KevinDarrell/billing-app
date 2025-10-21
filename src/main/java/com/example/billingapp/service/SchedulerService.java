package com.example.billingapp.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.YearMonth;

@Service
public class SchedulerService {

    private final ReminderService reminderService;
    private final ReportService reportService;

    public SchedulerService(ReminderService reminderService, ReportService reportService) {
        this.reminderService = reminderService;
        this.reportService = reportService;
    }

    @Scheduled(cron = "0 0 9 * * MON", zone = "Asia/Jakarta")
    public void scheduleWeeklyReminder() {
        System.out.println("🚀 Menjalankan scheduler: Reminder Mingguan...");
        

        reminderService.sendWeeklyReminders();
    }

    @Scheduled(cron = "0 0 9 2 * ?", zone = "Asia/Jakarta")
    public void scheduleMonthlyReport() {
        System.out.println("🚀 Menjalankan scheduler: Laporan Bulanan...");
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        int month = lastMonth.getMonthValue();
        int year = lastMonth.getYear();

        reportService.sendMonthlyReport(month, year);
    }
}