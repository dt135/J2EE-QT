package com.app.dangdoanhtoai2280603283.dto;

import com.app.dangdoanhtoai2280603283.model.Invoice;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO cho Order History response
 * - GET /orders/history: Lấy danh sách đơn hàng của user
 * - GET /orders/:id: Lấy chi tiết đơn hàng
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistoryResponse {

    private List<OrderSummary> orders;
    private Integer total;
    private Integer page;
    private Integer limit;
    private Integer totalPages;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderSummary {
        private String orderId;
        private String orderNumber;
        private LocalDateTime createdAt;
        private Double totalAmount;
        private String status;
        private Integer itemCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetail {
        private String orderId;
        private String orderNumber;
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
