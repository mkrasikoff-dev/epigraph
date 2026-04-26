package com.mkrasikoff.epigraph.service;

import com.mkrasikoff.epigraph.model.User;
import com.mkrasikoff.epigraph.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final QuoteService quoteService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       QuoteService quoteService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.quoteService = quoteService;
    }

    @Transactional
    public String register(String email, String rawPassword) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Этот email уже зарегистрирован");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setProvider("local");
        user.setCreatedAt(System.currentTimeMillis());

        userRepository.save(user); // userId генерируется здесь (IDENTITY strategy)

        quoteService.createDefaultQuotes(user.getId());

        return jwtService.generateToken(user.getId(), user.getEmail());
    }

    @Transactional(readOnly = true)
    public String login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .map(user -> jwtService.generateToken(user.getId(), user.getEmail()))
                .orElse(null);
    }
}
