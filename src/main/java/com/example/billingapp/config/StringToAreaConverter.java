package com.example.billingapp.config;

import com.example.billingapp.model.Area;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToAreaConverter implements Converter<String, Area> {

    @Override
    public Area convert(String source) {
        // Jika string yang datang dari form kosong atau null, kembalikan null
        if (source == null || source.isEmpty()) {
            return null;
        }
        try {
            // Ubah string menjadi enum
            return Area.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Jika string tidak cocok dengan enum manapun, kembalikan null
            return null;
        }
    }
}