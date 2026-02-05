package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.AdminOrderResponse;
import com.app.dangdoanhtoai2280603283.dto.ApiResponse;
import com.app.dangdoanhtoai2280603283.dto.PageResponse;
import com.app.dangdoanhtoai2280603283.dto.RevenueResponse;
import com.app.dangdoanhtoai2280603283.dto.UpdateOrderStatusRequest;
import com.app.dangdoanhtoai2280603283.model.Invoice;
import com.app.dangdoanhtoai2280603283.service.InvoiceService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller quan ly don hang cho ADMIN
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final InvoiceService invoiceService;

    /**
     * XEM DANH SACH DON HANG
     * GET /admin/orders
     * - Query params: status, fromDate, toDate, page, limit
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<List<AdminOrderResponse.InvoiceResponse>>>> getOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (fromDate != null) {
            startDate = LocalDateTime.parse(fromDate);
        }
        if (toDate != null) {
            endDate = LocalDateTime.parse(toDate);
        }

        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        Page<AdminOrderResponse.InvoiceResponse> orderPage = invoiceService.getAdminOrders(
                status, startDate, endDate, pageable);

        PageResponse<List<AdminOrderResponse.InvoiceResponse>> pageResponse = PageResponse
                .<List<AdminOrderResponse.InvoiceResponse>>builder()
                .content(orderPage.getContent())
                .page(page)
                .limit(limit)
                .total(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * XEM CHI TIET DON HANG
     * GET /admin/orders/:orderId
     */
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<ApiResponse<AdminOrderResponse.OrderDetailResponse>> getOrderDetail(
            @PathVariable String orderId) {
        AdminOrderResponse.OrderDetailResponse detail = invoiceService.getAdminOrderDetail(orderId);
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * CAP NHAT TRANG THAI DON HANG
     * PUT /admin/orders/:orderId/status
     */
    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<Invoice>> updateOrderStatus(
            @PathVariable String orderId,
            @RequestBody UpdateOrderStatusRequest request) {
        Invoice invoice = invoiceService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success("Cap nhat trang thai thanh cong", invoice));
    }

    /**
     * EXPORT DANH SACH DON HANG (CSV)
     * GET /admin/orders/export
     * - Query params: status, fromDate, toDate
     */
    @GetMapping("/orders/export")
    public void exportOrders(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            HttpServletResponse response) throws IOException {

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (fromDate != null) {
            startDate = LocalDateTime.parse(fromDate);
        }
        if (toDate != null) {
            endDate = LocalDateTime.parse(toDate);
        }

        List<AdminOrderResponse.InvoiceResponse> orders = invoiceService.getOrdersForExport(
                status, startDate, endDate);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=orders_export_"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");

        PrintWriter writer = response.getWriter();

        // CSV Header
        writer.println("Order ID,Order Number,Username,Email,Total Amount,Status,Created At,Item Count");

        // CSV Data
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (AdminOrderResponse.InvoiceResponse order : orders) {
            writer.println(
                    order.getOrderId() + "," +
                    order.getOrderNumber() + "," +
                    order.getUsername() + "," +
                    order.getEmail() + "," +
                    order.getTotalAmount() + "," +
                    order.getStatus() + "," +
                    (order.getCreatedAt() != null ? order.getCreatedAt().format(formatter) : "") + "," +
                    order.getItemCount()
            );
        }

        writer.flush();
        writer.close();
    }

    /**
     * THONG KE DOANH THU THEO THANG
     * GET /admin/revenue/monthly
     * - Query params: year
     */
    @GetMapping("/revenue/monthly")
    public ResponseEntity<ApiResponse<RevenueResponse>> getMonthlyRevenue(
            @RequestParam(required = false) Integer year) {
        RevenueResponse revenue = invoiceService.getMonthlyRevenue(year);
        return ResponseEntity.ok(ApiResponse.success(revenue));
    }
}
