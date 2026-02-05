package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.ApiResponse;
import com.app.dangdoanhtoai2280603283.dto.PageResponse;
import com.app.dangdoanhtoai2280603283.model.Book;
import com.app.dangdoanhtoai2280603283.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller (BAI 8)
 * - GET /api/books: Lay danh sach sach
 * - GET /api/books/:id: Lay chi tiet sach
 * - DELETE /api/books/:id: Xoa sach (ADMIN, can JWT Bearer Token)
 *
 * Authorization bang JWT Bearer Token
 * CORS da duoc cau hinh trong CorsConfig
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final BookService bookService;

    /**
     * GET /api/books
     * Lay danh sach sach (Public)
     */
    @GetMapping("/books")
    public ResponseEntity<ApiResponse<PageResponse<List<Book>>>> getBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        Page<Book> bookPage = bookService.getAllBooks(pageable);

        PageResponse<List<Book>> pageResponse = PageResponse.<List<Book>>builder()
                .content(bookPage.getContent())
                .page(page)
                .limit(limit)
                .total(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .build();

        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * GET /api/books/:id
     * Lay chi tiet sach (Public)
     */
    @GetMapping("/books/{id}")
    public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable String id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    /**
     * DELETE /api/books/:id
     * Xoa sach (ADMIN - can JWT Bearer Token)
     *
     * Header: Authorization: Bearer <jwt_token>
     */
    @DeleteMapping("/books/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Xoa sach thanh cong", null));
    }

    /**
     * POST /api/migrate-books
     * Migrate sach cu sang moi format (categoryId, categoryName)
     * Admin only
     */
    @PostMapping("/migrate-books")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> migrateBooks() {
        int migratedCount = bookService.migrateBooksToNewFormat();
        return ResponseEntity.ok(ApiResponse.success(
                "Migrated " + migratedCount + " books successfully",
                "Migrated " + migratedCount + " books"
        ));
    }
}
