package com.example.billingapp.service;

import com.example.billingapp.model.User;
import com.example.billingapp.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    // Gunakan constructor injection
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User tidak ditemukan: " + username));

        // ✅ PERBAIKAN UTAMA: Pastikan Anda me-return CustomUserDetails di sini.
        // Jangan gunakan 'new org.springframework.security.core.userdetails.User(...)'
        return new CustomUserDetails(user);
    }
}