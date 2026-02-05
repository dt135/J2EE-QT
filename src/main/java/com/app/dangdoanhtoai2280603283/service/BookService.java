package com.app.dangdoanhtoai2280603283.service;

import com.app.dangdoanhtoai2280603283.dto.BookRequest;
import com.app.dangdoanhtoai2280603283.exception.ResourceNotFoundException;
import com.app.dangdoanhtoai2280603283.model.Book;
import com.app.dangdoanhtoai2280603283.model.Category;
import com.app.dangdoanhtoai2280603283.repository.BookRepository;
import com.app.dangdoanhtoai2280603283.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service xu ly Book (BAI 3)
 * - CRUD sach
 * - Tim kiem theo title
 * - Loc theo gia
 */
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Populate categoryId và categoryName từ Category object
     * Dùng cho dữ liệu cũ (được tạo với @DBRef Category)
     */
    private void populateCategoryFields(Book book) {
        if (book != null) {
            // Nếu chưa có categoryId/categoryName nhưng có Category object
            if ((book.getCategoryId() == null || book.getCategoryId().isEmpty()) &&
                book.getCategory() != null) {
                book.setCategoryId(book.getCategory().getId());
                book.setCategoryName(book.getCategory().getName());
            }
        }
    }

    /**
     * Populate categoryId và categoryName cho list books
     */
    private List<Book> populateCategoryFieldsForList(List<Book> books) {
        return books.stream()
                .peek(this::populateCategoryFields)
                .collect(Collectors.toList());
    }

    /**
     * Populate categoryId và categoryName cho page books
     */
    private Page<Book> populateCategoryFieldsForPage(Page<Book> booksPage) {
        List<Book> content = populateCategoryFieldsForList(booksPage.getContent());
        return new PageImpl<>(
                content,
                booksPage.getPageable(),
                booksPage.getTotalElements()
        );
    }

    /**
     * Lay tat ca sach (phan trang)
     * GET /books
     */
    public Page<Book> getAllBooks(Pageable pageable) {
        Page<Book> booksPage = bookRepository.findAll(pageable);
        return populateCategoryFieldsForPage(booksPage);
    }

    /**
     * Lay chi tiet sach theo ID
     * GET /books/:id
     */
    public Book getBookById(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", id));
        populateCategoryFields(book);
        return book;
    }

    /**
     * Tim kiem sach theo title (case-insensitive, regex)
     * GET /books/search?keyword=
     */
    public List<Book> searchBooks(String keyword) {
        List<Book> books = bookRepository.searchByTitle(keyword);
        return populateCategoryFieldsForList(books);
    }

    /**
     * Loc sach theo gia toi da
     * GET /books/filter?price=&limit=
     */
    public List<Book> filterBooksByPrice(Double maxPrice, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Book> books = bookRepository.findByPriceLessThanEqualOrderByPriceAsc(maxPrice, pageable);
        return populateCategoryFieldsForList(books);
    }

    /**
     * Them sach moi (ADMIN)
     * POST /books
     */
    public Book createBook(BookRequest request) {
        // Tim category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .price(request.getPrice())
                .categoryId(request.getCategoryId())
                .categoryName(category.getName())
                .description(request.getDescription())
                .build();

        return bookRepository.save(book);
    }

    /**
     * Cap nhat sach (ADMIN)
     * PUT /books/:id
     */
    public Book updateBook(String id, BookRequest request) {
        Book book = getBookById(id);

        // Tim category moi (neu thay doi)
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            book.setCategoryId(request.getCategoryId());
            book.setCategoryName(category.getName());
        }

        if (request.getTitle() != null) {
            book.setTitle(request.getTitle());
        }
        if (request.getAuthor() != null) {
            book.setAuthor(request.getAuthor());
        }
        if (request.getPrice() != null) {
            book.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            book.setDescription(request.getDescription());
        }

        return bookRepository.save(book);
    }

    /**
     * Xoa sach (ADMIN)
     * DELETE /books/:id
     */
    public void deleteBook(String id) {
        Book book = getBookById(id);
        bookRepository.delete(book);
    }

    /**
     * Migrate sach cu sang moi format (categoryId, categoryName)
     * Dung cho du lieu cu duoc tao voi @DBRef Category
     */
    public int migrateBooksToNewFormat() {
        int migratedCount = 0;
        List<Book> allBooks = bookRepository.findAll();

        for (Book book : allBooks) {
            // Kiểm tra nếu sách chưa có categoryId
            if (book.getCategoryId() == null || book.getCategoryId().isEmpty()) {
                // Gán categoryId mặc định từ danh sách category đầu tiên
                List<Category> categories = categoryRepository.findAll();
                if (!categories.isEmpty()) {
                    Category defaultCategory = categories.get(0);
                    book.setCategoryId(defaultCategory.getId());
                    book.setCategoryName(defaultCategory.getName());
                    bookRepository.save(book);
                    migratedCount++;
                }
            }
        }

        return migratedCount;
    }
}
