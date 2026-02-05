package com.app.dangdoanhtoai2280603283.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO cho Book request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {
    
    @NotBlank(message = "Tieu de sach khong duoc de trong")
    @Size(max = 200, message = "Tieu de khong qua 200 ky tu")
    private String title;
    
    @NotBlank(message = "Tac gia khong duoc de trong")
    @Size(max = 100, message = "Ten tac gia khong qua 100 ky tu")
    private String author;
    
    @NotNull(message = "Gia sach khong duoc de trong")
    @Positive(message = "Gia sach phai lon hon 0")
    private Double price;
    
    @NotBlank(message = "Danh muc khong duoc de trong")
    private String categoryId;
    
    @Size(max = 2000, message = "Mo ta khong qua 2000 ky tu")
    private String description;
}
