package com.example.billingapp.repository;

import com.example.billingapp.model.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
    public interface AppSettingsRepository extends JpaRepository<AppSettings, String> {   
    }
