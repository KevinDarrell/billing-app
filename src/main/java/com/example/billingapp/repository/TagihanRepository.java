package com.example.billingapp.repository;

import com.example.billingapp.model.Area;
import com.example.billingapp.model.Tagihan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TagihanRepository extends JpaRepository<Tagihan, Long> {

    List<Tagihan> findByStatus(String status);
    List<Tagihan> findByTanggalDiterimaBetween(LocalDate startDate, LocalDate endDate);
    List<Tagihan> findByLokasiArea(Area area);
    Optional<Tagihan> findByIdAndLokasiArea(Long id, Area area);

    @Query("SELECT COUNT(t) FROM Tagihan t WHERE MONTH(t.tanggalDiterima) = :month AND YEAR(t.tanggalDiterima) = :year")
    Long countByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(t) FROM Tagihan t WHERE t.status = :status AND MONTH(t.tanggalDiterima) = :month AND YEAR(t.tanggalDiterima) = :year")
    Long countByStatusAndMonthAndYear(@Param("status") String status, @Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(t) FROM Tagihan t WHERE t.lokasi.area = :area AND MONTH(t.tanggalDiterima) = :month AND YEAR(t.tanggalDiterima) = :year")
    Long countByLokasiAreaAndMonthAndYear(@Param("area") Area area, @Param("month") int month, @Param("year") int year);

    @Query("SELECT COUNT(t) FROM Tagihan t WHERE t.status = :status AND t.lokasi.area = :area AND MONTH(t.tanggalDiterima) = :month AND YEAR(t.tanggalDiterima) = :year")
    Long countByStatusAndLokasiAreaAndMonthAndYear(@Param("status") String status, @Param("area") Area area, @Param("month") int month, @Param("year") int year);

    // ✅ TAMBAHKAN METHOD INI
    @Query("SELECT t FROM Tagihan t WHERE t.status = 'Belum Dibayar' AND t.lokasi.area = :area")
    List<Tagihan> findUnpaidByArea(@Param("area") Area area);

    @Query("SELECT t FROM Tagihan t WHERE t.lokasi.area = :area AND MONTH(t.tanggalDiterima) = :month AND YEAR(t.tanggalDiterima) = :year")
    List<Tagihan> findByLokasiAreaAndMonthAndYear(@Param("area") Area area, @Param("month") int month, @Param("year") int year);
}
