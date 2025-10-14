package com.example.billingapp.service;

import com.example.billingapp.model.Pembayaran;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {
    @Value("${app.upload.dir}")
    private String uploadDir;

    // generate PDF laporan --> returns path file pdf yang dibuat
    public String generateMonthlyReportPdf(int year, int month, List<Pembayaran> payments) throws IOException {
        if (payments == null || payments.isEmpty()) {
            return null;
        }

        String monthLabel = java.time.Month.of(month).name() + "_" + year;
        String outputDir = "reports/";
        File outDir = new File(outputDir);
        if (!outDir.exists()) outDir.mkdirs();

        String pdfFileName = outputDir + "rekap_tagihan_" + monthLabel + ".pdf";
        try (OutputStream os = new FileOutputStream(pdfFileName)) {
            String html = buildHtml(payments, year, month);
            PdfRendererBuilder builder = new PdfRendererBuilder();
            // baseUri supaya gambar file:/// bisa di-resolve (gunakan absolute path)
            String baseUri = new File(".").getAbsoluteFile().toURI().toString();
            builder.withHtmlContent(html, baseUri);
            builder.toStream(os);
            builder.run();
        } catch (Exception e) {
            throw new IOException("Gagal generate PDF: " + e.getMessage(), e);
        }
        return pdfFileName;
    }

    private String buildHtml(List<Pembayaran> payments, int year, int month) {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset='utf-8'/><style>");
        sb.append("table{width:100%;border-collapse:collapse;}td,th{border:1px solid #333;padding:6px}");
        sb.append("img{max-width:500px;max-height:600px;display:block;margin:10px 0}");
        sb.append("</style></head><body>");
        sb.append("<h2>Rekap Pembayaran - ").append(java.time.Month.of(month)).append(" ").append(year).append("</h2>");

        sb.append("<table><thead><tr><th>#</th><th>Vendor</th><th>Lokasi</th><th>Tanggal Tagihan</th><th>Nilai</th><th>Tanggal Bayar</th><th>Dibayar Oleh</th></tr></thead><tbody>");
        int i = 1;
        for (Pembayaran p : payments) {
            sb.append("<tr>")
              .append("<td>").append(i++).append("</td>")
              .append("<td>").append(escape(p.getTagihan().getVendor().getNamaVendor())).append("</td>")
              .append("<td>").append(escape(p.getTagihan().getLokasi().getNamaLokasi())).append("</td>")
              .append("<td>").append(p.getTagihan().getTanggalDiterima() != null ? p.getTagihan().getTanggalDiterima().format(df) : "-").append("</td>")
              .append("<td>").append(p.getTagihan().getNilaiPaymentVoucher()).append("</td>")
              .append("<td>").append(p.getTanggalPembayaran() != null ? p.getTanggalPembayaran().format(df) : "-").append("</td>")
              .append("<td>").append(escape(p.getDibayarOleh())).append("</td>")
              .append("</tr>");
        }
        sb.append("</tbody></table>");

        // Sisipkan bukti transfer gambar / file tiap pembayaran (jika ada)
        sb.append("<h3>Lampiran Bukti Transfer</h3>");
        i = 1;
        for (Pembayaran p : payments) {
            String path = p.getBuktiTransferPath();
            if (path != null && !path.isBlank()) {
                // make absolute path compatible for file URI (OpenHTMLToPDF reads via baseUri)
                File f = new File(path);
                if (f.exists()) {
                    sb.append("<div><h4>Bukti ").append(i++).append(" - Tagihan #").append(p.getTagihan().getId()).append("</h4>");
                    // For images, use <img src='file:/absolute/path'/>. For PDF attachments (if any), mention filename.
                    String uri = f.toURI().toString();
                    // If file is an image -> show it as image; otherwise show filename
                    String lower = f.getName().toLowerCase();
                    if (lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".gif")) {
                        sb.append("<img src='").append(uri).append("' alt='bukti'/>");
                    } else {
                        sb.append("<p>File bukti: ").append(f.getName()).append("</p>");
                    }
                    sb.append("</div>");
                } else {
                    sb.append("<p>Bukti tidak ditemukan untuk Tagihan #").append(p.getTagihan().getId()).append("</p>");
                }
            }
        }

        sb.append("</body></html>");
        return sb.toString();
    }

    // very small helper to avoid null in html
    private String escape(Object o) {
        return o == null ? "-" : o.toString().replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}