package com.app.dangdoanhtoai2280603283.repository;

import com.app.dangdoanhtoai2280603283.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Book Entity
 */
@Repository
public interface BookRepository extends MongoRepository<Book, String> {

    /**
     * Tim sach theo title (case-insensitive, regex)
     * BAI 3: GET /books/search?keyword=
     */
    @Query("{ 'title': { $regex: ?0, $options: 'i' } }")
    List<Book> searchByTitle(String keyword);

    /**
     * Tim sach co gia <= maxPrice, gioi han so luong
     * BAI 3: GET /books/filter?price=&limit=
     */
    List<Book> findByPriceLessThanEqualOrderByPriceAsc(Double maxPrice, Pageable pageable);

    /**
     * Lay sach theo category id
     */
    List<Book> findByCategoryId(String categoryId);

    /**
     * Dem so sach trong category
     */
    long countByCategoryId(String categoryId);

    /**
     * Xoa tat ca sach theo categoryId
     */
    void deleteByCategoryId(String categoryId);

    /**
     * Lay tat ca sach voi phan trang
     */
    Page<Book> findAll(Pageable pageable);
}
