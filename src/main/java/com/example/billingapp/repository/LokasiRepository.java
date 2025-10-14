package com.example.billingapp.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.example.billingapp.model.Lokasi;
import com.example.billingapp.model.Area;
import java.util.List;
import java.util.Optional;


public interface LokasiRepository extends JpaRepository<Lokasi, Long> {
    List<Lokasi> findByArea(Area area);
    long countByArea(Area area);

    Optional<Lokasi> findByIdAndArea(Long id, Area area);
}

