package com.app.dangdoanhtoai2280603283.repository;

import com.app.dangdoanhtoai2280603283.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho Cart Entity
 */
@Repository
public interface CartRepository extends MongoRepository<Cart, String> {
    
    /**
     * Tim cart theo userId
     * Moi user chi co 1 cart
     */
    Optional<Cart> findByUserId(String userId);
    
    /**
     * Xoa cart theo userId
     */
    void deleteByUserId(String userId);
}
