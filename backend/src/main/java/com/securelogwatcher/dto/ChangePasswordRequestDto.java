package com.securelogwatcher.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class ChangePasswordRequestDto {
    @NotBlank(message = "Password is required.")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,}$", message = "Password must be at least 8 characters and include uppercase, lowercase, number, and special characters !@#$%^&*().")
    private String newPassword;
    private String oldPassword;
}
