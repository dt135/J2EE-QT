package com.app.dangdoanhtoai2280603283.service;

import com.app.dangdoanhtoai2280603283.dto.CategoryRequest;
import com.app.dangdoanhtoai2280603283.exception.BadRequestException;
import com.app.dangdoanhtoai2280603283.exception.ResourceNotFoundException;
import com.app.dangdoanhtoai2280603283.model.Category;
import com.app.dangdoanhtoai2280603283.repository.BookRepository;
import com.app.dangdoanhtoai2280603283.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service xu ly Category (BAI 4)
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    /**
     * Lay tat ca danh muc
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    /**
     * Lay danh muc voi phan trang
     */
    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    /**
     * Lay chi tiet danh muc theo ID
     */
    public Category getCategoryById(String id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    /**
     * Tao danh muc moi (ADMIN)
     */
    public Category createCategory(CategoryRequest request) {
        // Kiem tra ten da ton tai chua
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Ten danh muc da ton tai");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        return categoryRepository.save(category);
    }

    /**
     * Cap nhat danh muc (ADMIN)
     */
    public Category updateCategory(String id, CategoryRequest request) {
        Category category = getCategoryById(id);

        // Kiem tra ten moi co trung khong (neu thay doi)
        if (!category.getName().equals(request.getName()) 
                && categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Ten danh muc da ton tai");
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());

        return categoryRepository.save(category);
    }

    /**
     * Xoa danh muc (ADMIN)
     * Chi xoa neu khong co sach nao trong danh muc
     */
    public void deleteCategory(String id) {
        Category category = getCategoryById(id);

        // Kiem tra xem co sach trong danh muc khong
        long bookCount = bookRepository.countByCategoryId(id);
        if (bookCount > 0) {
            throw new BadRequestException(
                    String.format("Khong the xoa danh muc dang chua %d cuon sach", bookCount));
        }

        categoryRepository.delete(category);
    }
}
