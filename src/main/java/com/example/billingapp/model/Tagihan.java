package com.example.billingapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Tagihan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne
    @JoinColumn(name = "vendor_id")
    private Company vendor;

    @ManyToOne
    @JoinColumn(name = "lokasi_id")
    private Lokasi lokasi;

    @OneToOne(mappedBy = "tagihan", cascade = CascadeType.ALL, orphanRemoval = true)
    private Pembayaran pembayaran;

    @Column(columnDefinition = "TEXT")
    private String note;

    private LocalDate tanggalDiterima;
    private Double nilaiPaymentVoucher;
    private String diterimaOleh;
    private String status; // "Belum Dibayar" atau "Sudah Dibayar"

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }
    
    public Company getCompany() { return company; }
    public void setCompany(Company company) { this.company = company; }

    public Company getVendor() { return vendor; }
    public void setVendor(Company vendor) { this.vendor = vendor; }

    public Lokasi getLokasi() { return lokasi; }
    public void setLokasi(Lokasi lokasi) { this.lokasi = lokasi; }

    public Pembayaran getPembayaran() { return pembayaran; }
    public void setPembayaran(Pembayaran pembayaran) { this.pembayaran = pembayaran; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public LocalDate getTanggalDiterima() { return tanggalDiterima; }
    public void setTanggalDiterima(LocalDate tanggalDiterima) { this.tanggalDiterima = tanggalDiterima; }

    public Double getNilaiPaymentVoucher() { return nilaiPaymentVoucher; }
    public void setNilaiPaymentVoucher(Double nilaiPaymentVoucher) { this.nilaiPaymentVoucher = nilaiPaymentVoucher; }

    public String getDiterimaOleh() { return diterimaOleh; }
    public void setDiterimaOleh(String diterimaOleh) { this.diterimaOleh = diterimaOleh; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}