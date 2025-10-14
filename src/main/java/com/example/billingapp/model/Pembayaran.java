package com.example.billingapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Pembayaran {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "tagihan_id")
    private Tagihan tagihan;

    private LocalDate tanggalPembayaran;
    private String buktiTransferPath; // nanti dipakai untuk upload file
    private String dibayarOleh;

    // Getter & Setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tagihan getTagihan() { return tagihan; }
    public void setTagihan(Tagihan tagihan) { this.tagihan = tagihan; }

    public LocalDate getTanggalPembayaran() { return tanggalPembayaran; }
    public void setTanggalPembayaran(LocalDate tanggalPembayaran) { this.tanggalPembayaran = tanggalPembayaran; }

    public String getBuktiTransferPath() { return buktiTransferPath; }
    public void setBuktiTransferPath(String buktiTransferPath) { this.buktiTransferPath = buktiTransferPath; }

    public String getDibayarOleh() { return dibayarOleh; }
    public void setDibayarOleh(String dibayarOleh) { this.dibayarOleh = dibayarOleh; }



}
