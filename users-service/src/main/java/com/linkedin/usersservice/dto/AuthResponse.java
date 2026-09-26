package com.linkedin.usersservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {

    private String userId;

    private String accessToken;
    private String refreshToken;
    private String email;
    private String firstName;
    private String lastName;
    private String tokenType = "Bearer";
}
