package com.example.billingapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.billingapp.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
