package com.app.dangdoanhtoai2280603283.dto;

import com.app.dangdoanhtoai2280603283.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho response sau khi dang nhap/dang ky
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    
    private String id;
    private String username;
    private String email;
    private Role role;
    private String token;
}
