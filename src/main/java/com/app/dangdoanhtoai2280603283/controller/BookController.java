package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.ApiResponse;
import com.app.dangdoanhtoai2280603283.dto.BookRequest;
import com.app.dangdoanhtoai2280603283.dto.PageResponse;
import com.app.dangdoanhtoai2280603283.model.Book;
import com.app.dangdoanhtoai2280603283.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xu ly Book (BAI 3)
 * - GET /books: Lay tat ca sach (Public)
 * - GET /books/search: Tim kiem sach (Public)
 * - GET /books/filter: Loc sach theo gia (Public)
 * - GET /books/:id: Lay chi tiet sach (Public)
 * - POST /books: Them sach (ADMIN)
 * - PUT /books/:id: Cap nhat sach (ADMIN)
 * - DELETE /books/:id: Xoa sach (ADMIN)
 */
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    /**
     * LAY TAT CA SACH (phan trang)
     * GET /books?page=0&limit=10&sortBy=createdAt&order=desc
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<List<Book>>>> getAllBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {
        
        Sort sort = order.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, limit, sort);
        
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
     * TIM KIEM SACH THEO TITLE (BAI 3)
     * GET /books/search?keyword=javascript
     * 
     * Tim kiem khong phan biet hoa thuong (regex)
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Map<String, Object>>> searchBooks(
            @RequestParam String keyword) {
        List<Book> books = bookService.searchBooks(keyword);
        
        Map<String, Object> data = new HashMap<>();
        data.put("keyword", keyword);
        data.put("count", books.size());
        data.put("books", books);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * LOC SACH THEO GIA (BAI 3)
     * GET /books/filter?price=100000&limit=5
     * 
     * Lay toi da K sach co gia <= price
     */
    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<Map<String, Object>>> filterBooks(
            @RequestParam Double price,
            @RequestParam(defaultValue = "10") int limit) {
        List<Book> books = bookService.filterBooksByPrice(price, limit);
        
        Map<String, Object> data = new HashMap<>();
        data.put("maxPrice", price);
        data.put("limit", limit);
        data.put("count", books.size());
        data.put("books", books);
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * LAY CHI TIET SACH
     * GET /books/:id
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Book>> getBookById(@PathVariable String id) {
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(ApiResponse.success(book));
    }

    /**
     * THEM SACH MOI (ADMIN)
     * POST /books
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Book>> createBook(
            @Valid @RequestBody BookRequest request) {
        Book book = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Them sach thanh cong", book));
    }

    /**
     * CAP NHAT SACH (ADMIN)
     * PUT /books/:id
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Book>> updateBook(
            @PathVariable String id,
            @Valid @RequestBody BookRequest request) {
        Book book = bookService.updateBook(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat sach thanh cong", book));
    }

    /**
     * XOA SACH (ADMIN)
     * DELETE /books/:id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String id) {
        bookService.deleteBook(id);
        return ResponseEntity.ok(ApiResponse.success("Xoa sach thanh cong", null));
    }
}
