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
     * Sets the user's password.
     * No current-password check — account recovery is handled by the
     * forgot-password / email-verification flow. After this call the
     * user can always log in locally, so provider is normalised to "local".
     */
    @Transactional
    public void changePassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setProvider("local");

        userRepository.save(user);
    }
}
