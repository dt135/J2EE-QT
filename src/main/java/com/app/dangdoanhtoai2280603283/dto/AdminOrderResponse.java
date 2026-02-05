package com.app.dangdoanhtoai2280603283.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderResponse {

    private String orderId;
    private String orderNumber;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private Double totalAmount;
    private String status;
    private Integer itemCount;

    public static AdminOrderResponse fromInvoice(InvoiceResponse invoice) {
        return AdminOrderResponse.builder()
                .orderId(invoice.getOrderId())
                .orderNumber(invoice.getOrderNumber())
                .username(invoice.getUsername())
                .email(invoice.getEmail())
                .createdAt(invoice.getCreatedAt())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .itemCount(invoice.getItemCount())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceResponse {
        private String orderId;
        private String orderNumber;
        private String username;
        private String email;
        private LocalDateTime createdAt;
        private Double totalAmount;
        private String status;
        private Integer itemCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailResponse {
        private String orderId;
        private String orderNumber;
        private String username;
        private String email;
        private LocalDateTime createdAt;
        private Double totalAmount;
        private String status;
        private List<OrderItemResponse> items;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemResponse {
        private String bookId;
        private String bookTitle;
        private String bookAuthor;
        private Double price;
        private Integer quantity;
        private Double subtotal;
    }
}
