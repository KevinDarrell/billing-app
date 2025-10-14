package com.example.billingapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry; // <-- 1. Tambahkan import ini
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Konfigurasi Anda yang sudah ada (biarkan saja)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadDir = System.getProperty("user.dir") + "/uploads/";
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir);
    }

    // ✅ 2. TAMBAHAN: Daftarkan konverter String ke Enum Area di sini
    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new StringToAreaConverter());
    }
}