package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.ApiResponse;
import com.app.dangdoanhtoai2280603283.dto.CategoryRequest;
import com.app.dangdoanhtoai2280603283.dto.PageResponse;
import com.app.dangdoanhtoai2280603283.model.Category;
import com.app.dangdoanhtoai2280603283.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller xu ly Category (BAI 4)
 * - GET /categories: Lay tat ca danh muc (Public)
 * - GET /categories/:id: Lay chi tiet danh muc (Public)
 * - POST /categories: Tao danh muc (ADMIN)
 * - PUT /categories/:id: Cap nhat danh muc (ADMIN)
 * - DELETE /categories/:id: Xoa danh muc (ADMIN)
 */
@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * LAY TAT CA DANH MUC
     * GET /categories
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<List<Category>>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit);
        Page<Category> categoryPage = categoryService.getAllCategories(pageable);
        
        PageResponse<List<Category>> pageResponse = PageResponse.<List<Category>>builder()
                .content(categoryPage.getContent())
                .page(page)
                .limit(limit)
                .total(categoryPage.getTotalElements())
                .totalPages(categoryPage.getTotalPages())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * LAY CHI TIET DANH MUC
     * GET /categories/:id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Category>> getCategoryById(@PathVariable String id) {
        Category category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(category));
    }

    /**
     * TAO DANH MUC MOI (ADMIN)
     * POST /categories
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> createCategory(
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.createCategory(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tao danh muc thanh cong", category));
    }

    /**
     * CAP NHAT DANH MUC (ADMIN)
     * PUT /categories/:id
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Category>> updateCategory(
            @PathVariable String id,
            @Valid @RequestBody CategoryRequest request) {
        Category category = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat danh muc thanh cong", category));
    }

    /**
     * XOA DANH MUC (ADMIN)
     * DELETE /categories/:id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Xoa danh muc thanh cong", null));
    }
}
