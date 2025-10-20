package com.example.billingapp.model;

import jakarta.persistence.*;

@Entity
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String namaVendor;
    private String emailVendor;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNamaVendor() { return namaVendor; }
    public void setNamaVendor(String namaVendor) { this.namaVendor = namaVendor; }

    public String getEmailVendor() { return emailVendor; }
    public void setEmailVendor(String emailVendor) { this.emailVendor = emailVendor; }
}
