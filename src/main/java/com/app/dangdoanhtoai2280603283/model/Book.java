package com.app.dangdoanhtoai2280603283.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.TextIndexed;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Book Entity - Thong tin sach
 * Hỗ trợ cả 2 format:
 * - Dữ liệu cũ: @DBRef Category object
 * - Dữ liệu mới: categoryId và categoryName
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "books")
public class Book {

    @Id
    private String id;

    @NotBlank(message = "Tieu de sach khong duoc de trong")
    @Size(max = 200, message = "Tieu de khong qua 200 ky tu")
    @TextIndexed
    private String title;

    @NotBlank(message = "Tac gia khong duoc de trong")
    @Size(max = 100, message = "Ten tac gia khong qua 100 ky tu")
    @TextIndexed
    private String author;

    @NotNull(message = "Gia sach khong duoc de trong")
    @Positive(message = "Gia sach phai lon hon 0")
    private Double price;

    // ===== CATEGORY FIELDS - Hỗ trợ cả 2 format =====

    // Dữ liệu cũ: @DBRef Category object
    @DBRef
    @JsonIgnore
    private Category category;

    // Dữ liệu mới: String categoryId và categoryName
    private String categoryId;
    private String categoryName;

    @Size(max = 2000, message = "Mo ta khong qua 2000 ky tu")
    private String description;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
