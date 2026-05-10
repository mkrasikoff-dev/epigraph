package com.mkrasikoff.epigraph.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {

    /** Null for Google users who are setting a password for the first time. */
    private String currentPassword;

    @NotBlank
    @Size(min = 8, message = "Пароль должен содержать минимум 8 символов")
    private String newPassword;
}
