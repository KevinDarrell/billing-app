package com.example.billingapp.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.billingapp.model.Area;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.billingapp.model.Pembayaran;
import java.time.LocalDate;
import java.util.List;

public interface PembayaranRepository extends JpaRepository<Pembayaran, Long> {
    List<Pembayaran> findByTanggalPembayaranBetween(LocalDate start, LocalDate end);

    @Query("SELECT p FROM Pembayaran p WHERE p.tagihan.lokasi.area = :area")
    List<Pembayaran> findAllByArea(@Param("area") Area area);

}
