package com.example.billingapp.repository;

import com.example.billingapp.model.Area;
import com.example.billingapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // ✅ TAMBAHKAN METHOD INI
    // Mencari user yang merupakan Kepala Area di area tertentu.
    Optional<User> findByAreaAndIsAreaHead(Area area, boolean isAreaHead);
}