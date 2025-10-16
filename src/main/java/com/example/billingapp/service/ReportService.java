package com.example.billingapp.service;

import com.example.billingapp.model.Area;
import com.example.billingapp.model.AppSettings;
import com.example.billingapp.model.Pembayaran;
import com.example.billingapp.model.Tagihan;
import com.example.billingapp.model.User;
import com.example.billingapp.repository.AppSettingsRepository;
import com.example.billingapp.repository.TagihanRepository;
import com.example.billingapp.repository.UserRepository;
import com.example.billingapp.repository.AreaRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class ReportService {

    private final TagihanRepository tagihanRepository;
    private final EmailService emailService;
    private final AppSettingsRepository appSettingsRepository;
    private final UserRepository userRepository;
    private final AreaRepository areaRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${server.port}")
    private String serverPort;

    public ReportService(TagihanRepository tagihanRepository, EmailService emailService,
                         AppSettingsRepository appSettingsRepository, UserRepository userRepository, AreaRepository areaRepository) {
        this.tagihanRepository = tagihanRepository;
        this.emailService = emailService;
        this.appSettingsRepository = appSettingsRepository;
        this.userRepository = userRepository;
        this.areaRepository = areaRepository;
    }

    public void sendMonthlyReport(int month, int year) {
        List<Area> areas = areaRepository.findAll();
        String emailTo = appSettingsRepository.findById("email_recipient_to")
                .map(AppSettings::getSettingValue).orElse("");

        if (!StringUtils.hasText(emailTo)) {
            System.out.println("❌ GAGAL: Email tujuan utama (To) tidak diatur di Pengaturan.");
            return;
        }

        for (Area area : areas) {
            List<Tagihan> tagihanAreaIni = tagihanRepository.findByLokasiAreaAndMonthAndYear(area, month, year);

            if (tagihanAreaIni.isEmpty()) {
                System.out.println("INFO: Tidak ada tagihan untuk area " + area.getName() + ". Email tidak dikirim.");
                continue;
            }

            Optional<User> headUserOpt = userRepository.findByAreaAndIsAreaHead(area, true);
            String emailCcHead = headUserOpt.map(User::getEmail).orElse("");

            try {
                String bulan = LocalDate.of(year, month, 1).getMonth().getDisplayName(TextStyle.FULL, Locale.forLanguageTag("id-ID"));
                String tahun = String.valueOf(year);
                String subject = "Laporan Pembayaran Area " + area.getName() + " - " + bulan + " " + tahun;

                List<File> attachments = new ArrayList<>();
                File pdfFile = createPdfForArea(tagihanAreaIni, area, bulan, tahun, attachments);
                attachments.add(pdfFile);

                String htmlBody = createHtmlBodyForArea(tagihanAreaIni, area, bulan, tahun);
                
                List<String> recipients = new ArrayList<>();
                recipients.add(emailTo);
                if (StringUtils.hasText(emailCcHead)) {
                    recipients.add(emailCcHead);
                }

                emailService.sendEmailWithAttachments(recipients.toArray(new String[0]), subject, htmlBody, attachments);
                System.out.println("✅ Laporan untuk area " + area.getName() + " berhasil dikirim ke: " + String.join(", ", recipients));

            } catch (Exception e) {
                System.err.println("❌ GAGAL mengirim laporan untuk area " + area.getName() + ": " + e.getMessage());
            }
        }
    }

    private File createPdfForArea(List<Tagihan> tagihans, Area area, String bulan, String tahun, List<File> attachments) throws DocumentException, IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) dir.mkdirs();
        File pdfFile = new File(dir, "laporan_" + area.getName() + "_" + bulan + "_" + tahun + ".pdf");
        
        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Laporan Tagihan Area " + area.getName() + " - " + bulan + " " + tahun, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        PdfPTable table = new PdfPTable(9);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{2, 3, 3, 3, 3, 3, 2, 3, 3});
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

        table.addCell(new PdfPCell(new Phrase("Area", headFont)));
        table.addCell(new PdfPCell(new Phrase("Vendor", headFont)));
        table.addCell(new PdfPCell(new Phrase("Company", headFont)));
        table.addCell(new PdfPCell(new Phrase("No. Invoice", headFont)));
        table.addCell(new PdfPCell(new Phrase("Lokasi", headFont)));
        table.addCell(new PdfPCell(new Phrase("Kota/Kab", headFont)));
        table.addCell(new PdfPCell(new Phrase("Tgl Bayar", headFont)));
        table.addCell(new PdfPCell(new Phrase("Jumlah", headFont)));
        table.addCell(new PdfPCell(new Phrase("Bukti Bayar", headFont)));

        for (Tagihan t : tagihans) {
            table.addCell(t.getLokasi().getArea().getName());
            table.addCell(t.getVendor().getNamaVendor());
            table.addCell(t.getLokasi().getCompany());
            table.addCell(t.getInvoiceNumber());
            table.addCell(t.getLokasi().getNamaLokasi());
            table.addCell(t.getLokasi().getKota());

            Pembayaran p = t.getPembayaran();
            table.addCell(p != null ? p.getTanggalPembayaran().toString() : "Belum Dibayar");
            table.addCell(formatRupiah(t.getNilaiPaymentVoucher()));

            String buktiPath = (p != null) ? p.getBuktiTransferPath() : null;

            if (StringUtils.hasText(buktiPath)) {
                String fileUrl = "http://localhost:" + serverPort + "/uploads/" + buktiPath;
                Chunk link = new Chunk("Lihat Bukti");
                link.setAnchor(fileUrl);
                PdfPCell cell = new PdfPCell(new Phrase(link));
                table.addCell(cell);
            File buktiFile = new File(uploadDir + File.separator + buktiPath);
                if (buktiFile.exists()) {
                    attachments.add(buktiFile);
                }
            } else {
                table.addCell("-");
            }
        }
        document.add(table);
        document.close();
        
        // ✅ PERBAIKAN: File PDF utama juga harus ditambahkan ke lampiran
        attachments.add(pdfFile);
        
        return pdfFile;
    }

    private String createHtmlBodyForArea(List<Tagihan> tagihans, Area area, String bulan, String tahun) {
        StringBuilder htmlBody = new StringBuilder();
        htmlBody.append("<p>Yth. Bapak/Ibu,</p>");
        htmlBody.append("<p>Berikut adalah ringkasan Laporan Pembayaran untuk area <strong>")
                .append(area.getName()).append("</strong> bulan ").append(bulan).append(" ").append(tahun).append(".</p>");
        htmlBody.append("<p>Detail lengkap terlampir dalam file PDF.</p><br>");

        htmlBody.append("<table border='1' cellpadding='6' cellspacing='0' style='border-collapse: collapse; font-family: sans-serif; font-size: 12px;'>");
        htmlBody.append("<tr style='background-color:#f2f2f2; text-align:left;'>")
                .append("<th>Area</th><th>Vendor</th><th>Company</th><th>No Invoice</th><th>Lokasi</th><th>Kota/Kab</th><th>Tanggal Bayar</th><th>Jumlah</th><th>Bukti Bayar</th></tr>");

        for (Tagihan t : tagihans) {
            String style = "";
            if ("Belum Dibayar".equalsIgnoreCase(t.getStatus())) {
            style = " style='background-color: #ff0000ff;'"; // Warna merah muda
        }
            htmlBody.append("<tr>");
            htmlBody.append("<td>").append(t.getLokasi().getArea().getName()).append("</td>");
            htmlBody.append("<td>").append(t.getVendor().getNamaVendor()).append("</td>");
            htmlBody.append("<td>").append(t.getLokasi().getCompany()).append("</td>");
            htmlBody.append("<td>").append(t.getInvoiceNumber()).append("</td>");
            htmlBody.append("<td>").append(t.getLokasi().getNamaLokasi()).append("</td>");
            htmlBody.append("<td>").append(t.getLokasi().getKota()).append("</td>");

            Pembayaran p = t.getPembayaran();
            if (p != null) {
            htmlBody.append("<td>").append(p.getTanggalPembayaran().toString()).append("</td>");
            } else {
            htmlBody.append("<td>Belum Dibayar</td>");
            }

        htmlBody.append("<td>").append(formatRupiah(t.getNilaiPaymentVoucher())).append("</td>");
        // ... (logika link bukti bayar Anda)
            String buktiPath = t.getPembayaran() != null ? t.getPembayaran().getBuktiTransferPath() : null;
            if (StringUtils.hasText(buktiPath)) {
                String fileUrl = "http://localhost:" + serverPort + "/uploads/" + buktiPath;
                htmlBody.append("<td><a href='").append(fileUrl).append("' target='_blank'>Lihat Bukti</a></td>");
            } else {
                htmlBody.append("<td>-</td>");
            }
            htmlBody.append("</tr>");
        }
        htmlBody.append("</table>");
        htmlBody.append("<br><p>Terima kasih.</p>");
        
        return htmlBody.toString();
    }
    
    private String formatRupiah(Double nilai) {
        if (nilai == null) return "-";
        return "Rp " + String.format("%,.0f", nilai).replace(",", ".");
    }
}