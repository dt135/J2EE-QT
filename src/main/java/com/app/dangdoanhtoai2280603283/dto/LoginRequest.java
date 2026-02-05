package com.app.dangdoanhtoai2280603283.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request dang nhap
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Username hoac email khong duoc de trong")
    private String username;

    @NotBlank(message = "Password khong duoc de trong")
    private String password;
}
