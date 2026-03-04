package com.tavinki.taskiu.modules.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    @Email(message = "Email structure is invalid")
    private String email;

    // 1. Length check
    @Size(min = 6, max = 20, message = "Password length must be between 6 and 20 characters")
    // 2. Lowercase check (note the surrounding .*)
    @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter")

    // 3. Uppercase check
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")

    // 4. Digit check (escape \ as \\ in Java strings)
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    private String password;

    @JsonProperty("turnstile_token")
    private String turnstileToken;

}
