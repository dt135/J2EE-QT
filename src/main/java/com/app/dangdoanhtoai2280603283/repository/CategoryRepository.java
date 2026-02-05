package com.app.dangdoanhtoai2280603283.repository;

import com.app.dangdoanhtoai2280603283.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho Category Entity
 */
@Repository
public interface CategoryRepository extends MongoRepository<Category, String> {
    
    /**
     * Tim category theo ten
     */
    Optional<Category> findByName(String name);
    
    /**
     * Kiem tra ten category da ton tai chua
     */
    boolean existsByName(String name);
}
