package com.example.billingapp.model;

import jakarta.persistence.*;

@Entity
public class Lokasi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "area_id")
    private Area area;

    private String company;
    private String namaLokasi;
    private String alamat;
    private String kota;

 
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }

    public String getNamaLokasi() { return namaLokasi; }
    public void setNamaLokasi(String namaLokasi) { this.namaLokasi = namaLokasi; }

    public String getAlamat() { return alamat; }
    public void setAlamat(String alamat) { this.alamat = alamat; }

    public String getKota() { return kota; }
    public void setKota(String kota) { this.kota = kota; }

    public Area getArea() { return area; }
    public void setArea(Area area) { this.area = area; }
}
