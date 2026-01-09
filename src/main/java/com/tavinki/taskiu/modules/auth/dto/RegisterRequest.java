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

    // 1. 長度檢查
    @Size(min = 6, max = 20, message = "Password length must be between 6 and 20 characters")
    // 2. 小寫檢查 (注意前後的 .*)
    @Pattern(regexp = ".*[a-z].*", message = "Password must contain at least one lowercase letter")

    // 3. 大寫檢查
    @Pattern(regexp = ".*[A-Z].*", message = "Password must contain at least one uppercase letter")

    // 4. 數字檢查 (Java 字串中 \ 需轉義為 \\)
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least one digit")
    private String password;

    @JsonProperty("turnstile_token")
    private String turnstileToken;

}
