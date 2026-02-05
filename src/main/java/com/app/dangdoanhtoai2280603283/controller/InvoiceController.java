package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.ApiResponse;
import com.app.dangdoanhtoai2280603283.dto.CheckoutResponse;
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
