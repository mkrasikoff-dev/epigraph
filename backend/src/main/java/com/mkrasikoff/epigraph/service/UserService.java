package com.mkrasikoff.epigraph.service;

import com.mkrasikoff.epigraph.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Deletes the user and all associated data (quotes cascade via FK).
     */
    @Transactional
    public void deleteAccount(Long userId) {
        userRepository.deleteById(userId);
    }
}
