package com.example.billingapp.service;

import com.example.billingapp.model.Tagihan;
import com.example.billingapp.model.User;
import com.example.billingapp.repository.TagihanRepository;

import jakarta.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



@Service
public class ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderService.class);
    private final TagihanRepository tagihanRepository;
    private final EmailService emailService;

    public ReminderService(TagihanRepository tagihanRepository, EmailService emailService) {
        this.tagihanRepository = tagihanRepository;
        this.emailService = emailService;
    }

    public void sendWeeklyReminders() {
        log.info("Memulai proses pengiriman reminder mingguan...");
        
        List<Tagihan> unpaidTagihans = tagihanRepository.findByStatus("Belum Dibayar");

        if (unpaidTagihans.isEmpty()) {
            log.info("Tidak ada tagihan yang belum dibayar. Reminder tidak dikirim.");
            return;
        }

        Map<User, List<Tagihan>> groupedByUser = unpaidTagihans.stream()
                .filter(t -> t.getCreatedBy() != null && t.getCreatedBy().getEmail() != null)
                .collect(Collectors.groupingBy(Tagihan::getCreatedBy));

        for (Map.Entry<User, List<Tagihan>> entry : groupedByUser.entrySet()) {
            User user = entry.getKey();
            List<Tagihan> userTagihans = entry.getValue();

            String subject = "⏰ Pengingat: " + userTagihans.size() + " Tagihan Belum Dibayar";
            StringBuilder body = new StringBuilder();

            body.append("<p>Halo, <b>").append(user.getUsername()).append("</b>,</p>");
            body.append("<p>Berikut adalah daftar tagihan yang Anda buat dan statusnya masih 'Belum Dibayar':</p>");
            body.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse: collapse; font-family: sans-serif; font-size: 12px;'>");
            
            // ✅ PERBAIKAN: Urutan header tabel disesuaikan
            body.append("<tr style='background-color:#f2f2f2;'>")
                .append("<th>Area</th>")
                .append("<th>Vendor</th>")
                .append("<th>Company</th>")
                .append("<th>No Invoice</th>")
                .append("<th>Lokasi</th>")
                .append("<th>Kota/Kab</th>")
                .append("<th>Tanggal Diterima</th>")
                .append("<th>Jumlah</th>")
                .append("<th>Status</th>")
                .append("<th>Note</th>")
                .append("</tr>");

            for (Tagihan t : userTagihans) {

                String style = "";
            if ("Belum Dibayar".equalsIgnoreCase(t.getStatus())) {
                style = " style='background-color: #ff0000ff;'"; // Warna merah muda
            }
                // ✅ PERBAIKAN: Urutan data disesuaikan dengan header
                body.append("<tr>")
                    .append("<td>").append(t.getLokasi().getArea().getName()).append("</td>")
                    .append("<td>").append(t.getVendor().getNamaVendor()).append("</td>")
                    .append("<td>").append(t.getLokasi().getCompany()).append("</td>")
                    .append("<td>").append(t.getInvoiceNumber()).append("</td>")
                    .append("<td>").append(t.getLokasi().getNamaLokasi()).append("</td>")
                    .append("<td>").append(t.getLokasi().getKota()).append("</td>")
                    .append("<td>").append(t.getTanggalDiterima()).append("</td>")
                    .append("<td>").append(formatRupiah(t.getNilaiPaymentVoucher())).append("</td>")
                    .append("<td><b>").append(t.getStatus()).append("</b></td>")
                    .append("<td>").append(t.getNote() != null ? t.getNote() : "-").append("</td>")
                    .append("</tr>");
            }

            body.append("</table><br>");
            body.append("<p>Mohon segera tindak lanjuti pembayaran ke vendor terkait.</p>");
            body.append("<p>Terima kasih.<br><br><em>Sistem Billing Otomatis</em></p>");

            try {
                emailService.sendHtmlEmail(user.getEmail(), subject, body.toString());
                log.info("📧 Reminder mingguan dikirim ke {} untuk {} tagihan.", user.getEmail(), userTagihans.size());
            } catch (MessagingException e) {
                log.error("❌ Gagal mengirim reminder ke {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    private String formatRupiah(Double nilai) {
        if (nilai == null) return "-";
        java.text.DecimalFormat df = new java.text.DecimalFormat("Rp #,##0");
        return df.format(nilai);
    }
}