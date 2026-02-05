package com.app.dangdoanhtoai2280603283.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;

/**
 * Item Entity - Chi tiet hoa don
 * Luu gia sach tai thoi diem mua
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "items")
public class Item {

    @Id
    private String id;

    @Indexed
    private String invoiceId;

    @DBRef
    private Book book;

    private String bookId;

    // Gia tai thoi diem mua
    private Double price;

    private Integer quantity;

    @CreatedDate
    private LocalDateTime createdAt;

    /**
     * Tinh thanh tien cho item
     */
    public Double getSubtotal() {
        return price * quantity;
    }
}
