package com.app.dangdoanhtoai2280603283.dto;

import com.app.dangdoanhtoai2280603283.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho Cart response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {
    
    private String id;
    private String userId;
    private List<CartItemResponse> items;
    private Double totalAmount;
    private Integer itemCount;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
        private String bookId;
        private Book book;
        private Integer quantity;
        private Double subtotal;
    }
}
