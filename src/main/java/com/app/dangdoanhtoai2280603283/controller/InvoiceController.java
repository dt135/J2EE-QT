package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.ApiResponse;
import com.app.dangdoanhtoai2280603283.dto.CheckoutResponse;
import com.app.dangdoanhtoai2280603283.dto.OrderHistoryResponse;
import com.app.dangdoanhtoai2280603283.dto.PageResponse;
import com.app.dangdoanhtoai2280603283.model.Invoice;
import com.app.dangdoanhtoai2280603283.model.Item;
import com.app.dangdoanhtoai2280603283.model.User;
import com.app.dangdoanhtoai2280603283.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xu ly Checkout & Invoice (BAI 5)
 * - POST /checkout: Thanh toan gio hang
 * - GET /orders/history: Xem lich su don hang (USER)
 * - GET /orders/:id: Xem chi tiet don hang (USER)
 * - GET /invoices: Xem lich su hoa don
 * - GET /invoices/all: Xem tat ca hoa don (ADMIN)
 * - GET /invoices/:id: Xem chi tiet hoa don
 */
@RestController
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    /**
     * THANH TOAN GIO HANG (CHECKOUT)
     * POST /checkout
     *
     * Luong xu ly:
     * 1. Lay gio hang
     * 2. Tao Invoice
     * 3. Tao Items
     * 4. Xoa gio hang
     *
     * Chi USER duoc phep checkout, ADMIN khong
     */
    @PostMapping("/checkout")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CheckoutResponse response = invoiceService.checkout(user.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thanh toan thanh cong", response));
    }

    /**
     * XEM LICH SU DON HANG CUA USER
     * GET /orders/history
     *
     * Chuyen muc:
     * - Chao USER co the xem don hang cua chinh minh
     * - Sap xep theo createdAt giam dan
     * - Phan trang de xu ly nhieu don hang
     */
    @GetMapping("/orders/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderHistoryResponse>> getOrderHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        User user = (User) authentication.getPrincipal();
        OrderHistoryResponse response = invoiceService.getOrderHistory(user.getId(), page, limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * XEM CHI TIET DON HANG
     * GET /orders/:id
     *
     * Chi USER co the xem chi tiet don hang cua chinh minh
     */
    @GetMapping("/orders/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<OrderHistoryResponse.OrderDetail>> getOrderDetail(
            @PathVariable String id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        OrderHistoryResponse.OrderDetail detail = invoiceService.getOrderDetail(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success(detail));
    }

    /**
     * DA NHAN DUOC HANG (USER)
     * PUT /orders/:id/received
     *
     * User danh dau don hang la da nhan duoc
     * Trạng thái đơn hàng sẽ cập nhật thành COMPLETED
     * Tính vào doanh thu
     */
    @PutMapping("/orders/{id}/received")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> markOrderAsReceived(
            @PathVariable String id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        invoiceService.markOrderAsReceived(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Đã xác nhận nhận hàng thành công!", null));
    }

    /**
     * XEM LICH SU HOA DON CUA USER
     * GET /invoices
     */
    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<PageResponse<List<Invoice>>>> getInvoices(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        User user = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        
        Page<Invoice> invoicePage = invoiceService.getInvoicesByUser(user.getId(), pageable);
        
        PageResponse<List<Invoice>> pageResponse = PageResponse.<List<Invoice>>builder()
                .content(invoicePage.getContent())
                .page(page)
                .limit(limit)
                .total(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * XEM TAT CA HOA DON (ADMIN)
     * GET /invoices/all
     */
    @GetMapping("/invoices/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<List<Invoice>>>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        
        Page<Invoice> invoicePage = invoiceService.getAllInvoices(pageable);
        
        PageResponse<List<Invoice>> pageResponse = PageResponse.<List<Invoice>>builder()
                .content(invoicePage.getContent())
                .page(page)
                .limit(limit)
                .total(invoicePage.getTotalElements())
                .totalPages(invoicePage.getTotalPages())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * XEM CHI TIET HOA DON
     * GET /invoices/:id
     */
    @GetMapping("/invoices/{id}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getInvoiceById(
            @PathVariable String id,
            Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        boolean isAdmin = user.getRole().name().equals("ADMIN");
        
        Invoice invoice = invoiceService.getInvoiceById(id, user.getId(), isAdmin);
        List<Item> items = invoiceService.getInvoiceItems(id);
        
        Map<String, Object> data = new HashMap<>();
        data.put("invoice", invoice);
        data.put("items", items);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
