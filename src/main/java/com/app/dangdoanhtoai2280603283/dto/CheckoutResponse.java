package com.app.dangdoanhtoai2280603283.dto;

import com.app.dangdoanhtoai2280603283.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO cho Checkout response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponse {
    
    private Invoice invoice;
    private List<ItemResponse> items;
    private CheckoutSummary summary;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ItemResponse {
        private String bookId;
        private String bookTitle;
        private Double price;
        private Integer quantity;
        private Double subtotal;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CheckoutSummary {
        private Integer totalItems;
        private Integer totalQuantity;
        private Double totalAmount;
    }
}
