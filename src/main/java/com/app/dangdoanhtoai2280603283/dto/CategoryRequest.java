package com.app.dangdoanhtoai2280603283.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Category request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "Ten danh muc khong duoc de trong")
    @Size(max = 100, message = "Ten danh muc khong qua 100 ky tu")
    private String name;
    
    @Size(max = 500, message = "Mo ta khong qua 500 ky tu")
    private String description;
}
