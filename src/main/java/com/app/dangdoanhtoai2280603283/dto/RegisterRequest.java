package com.app.dangdoanhtoai2280603283.dto;

import com.app.dangdoanhtoai2280603283.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho request dang ky
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    
    @NotBlank(message = "Username khong duoc de trong")
    @Size(min = 3, max = 30, message = "Username phai tu 3-30 ky tu")
    private String username;
    
    @NotBlank(message = "Email khong duoc de trong")
    @Email(message = "Email khong hop le")
    private String email;
    
    @NotBlank(message = "Password khong duoc de trong")
    @Size(min = 6, message = "Password phai co it nhat 6 ky tu")
    private String password;
    
    private Role role;
}
