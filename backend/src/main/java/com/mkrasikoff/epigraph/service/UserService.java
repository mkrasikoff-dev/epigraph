package com.mkrasikoff.epigraph.service;

import com.mkrasikoff.epigraph.model.User;
import com.mkrasikoff.epigraph.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void deleteAccount(Long userId) {
        userRepository.deleteById(userId);
    }

    /**
     * Changes or sets the user's password.
     * Local users must provide the correct current password.
     * Google users (no password) can set one directly.
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        boolean hasPassword = user.getPassword() != null;

        if (hasPassword) {
            if (currentPassword == null || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new IllegalArgumentException("Неверный текущий пароль");
            }

            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                throw new IllegalArgumentException("Новый пароль совпадает с текущим");
            }
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        // If Google user sets a password — also enable local login
        if (!hasPassword) {
            user.setProvider("local");
        }

        userRepository.save(user);
    }
}
