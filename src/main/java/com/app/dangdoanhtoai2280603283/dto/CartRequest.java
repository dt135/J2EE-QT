package com.app.dangdoanhtoai2280603283.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Cart request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartRequest {
    
    @NotBlank(message = "Book ID khong duoc de trong")
    private String bookId;
    
    @Min(value = 1, message = "So luong phai it nhat la 1")
    @Builder.Default
    private Integer quantity = 1;
}
