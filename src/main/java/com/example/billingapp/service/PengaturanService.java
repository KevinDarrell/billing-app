package com.example.billingapp.service;

import com.example.billingapp.model.Area;
import com.example.billingapp.model.AppSettings;
import com.example.billingapp.model.User;
import com.example.billingapp.repository.AppSettingsRepository;
import com.example.billingapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PengaturanService {
    private final AppSettingsRepository appSettingsRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public PengaturanService(AppSettingsRepository appSettingsRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.appSettingsRepository = appSettingsRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, String> getEmailSettings() {
        Map<String, String> settings = new HashMap<>();
        List<AppSettings> emailSettings = appSettingsRepository.findAll().stream()
                .filter(s -> s.getSettingKey().startsWith("email_"))
                .collect(Collectors.toList());

        for (AppSettings setting : emailSettings) {
            settings.put(setting.getSettingKey(), setting.getSettingValue());
        }
        return settings;
    }

    public void updateEmailSettings(String emailTo, String emailCc) {
        AppSettings toSetting = appSettingsRepository.findById("email_recipient_to")
                .orElse(new AppSettings("email_recipient_to", emailTo));
        toSetting.setSettingValue(emailTo);
        appSettingsRepository.save(toSetting);

        AppSettings ccSetting = appSettingsRepository.findById("email_recipient_cc")
                .orElse(new AppSettings("email_recipient_cc", emailCc));
        ccSetting.setSettingValue(emailCc);
        appSettingsRepository.save(ccSetting);
    }

    public void changeOwnPassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("Password saat ini salah");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void changePasswordByAdmin(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ✅ METHOD BARU: Untuk menyimpan perubahan Kepala Area
    @Transactional // ✅ PENTING: Agar semua operasi (update/save) berhasil atau gagal bersamaan
    public void updateAreaHeads(Map<String, String> params) {
        // Loop untuk setiap kemungkinan area dari form (misal: "head_DKI", "head_JABAR", dst.)
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("head_")) {
                // Ekstrak nama Area dari key form, misal "head_DKI" -> "DKI"
                Area area = Area.valueOf(entry.getKey().substring(5));
                String newHeadUserIdStr = entry.getValue();

                // 1. Cari & copot jabatan kepala area yang LAMA untuk area ini
                Optional<User> oldHeadOpt = userRepository.findByAreaAndIsAreaHead(area, true);
                if (oldHeadOpt.isPresent()) {
                    User oldHead = oldHeadOpt.get();
                    oldHead.setAreaHead(false);
                    userRepository.save(oldHead);
                }

                // 2. Angkat kepala area BARU jika ada yang dipilih dari form
                if (newHeadUserIdStr != null && !newHeadUserIdStr.isEmpty()) {
                    Long newHeadUserId = Long.parseLong(newHeadUserIdStr);
                    User newHead = userRepository.findById(newHeadUserId)
                            .orElseThrow(() -> new RuntimeException("User untuk kepala area baru tidak ditemukan"));
                    newHead.setAreaHead(true);
                    userRepository.save(newHead);
                }
            }
        }
    } }