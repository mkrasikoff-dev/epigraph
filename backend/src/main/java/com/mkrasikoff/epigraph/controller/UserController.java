package com.mkrasikoff.epigraph.controller;

import com.mkrasikoff.epigraph.dto.ChangePasswordRequest;
import com.mkrasikoff.epigraph.dto.ErrorResponse;
import com.mkrasikoff.epigraph.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Permanently deletes the authenticated user's account and all associated data.
     */
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal Long userId) {
        userService.deleteAccount(userId);
        log.info("Account deleted — userId = {}", userId);
    }

    /**
     * Changes or sets the authenticated user's password.
     * Google users can call this without providing a current password.
     */
    @PatchMapping("/me/password")
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal Long userId,
                                            @Valid @RequestBody ChangePasswordRequest request) {
        try {
            userService.changePassword(userId, request.getNewPassword());
            log.info("Password changed — userId = {}", userId);

            return ResponseEntity.ok(new ErrorResponse("Пароль успешно изменён"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}
