package com.app.dangdoanhtoai2280603283.repository;

import com.app.dangdoanhtoai2280603283.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho User Entity
 */
@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    /**
     * Tim user theo username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Tim user theo email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Tim user theo googleId
     */
    Optional<User> findByGoogleId(String googleId);
    
    /**
     * Kiem tra username da ton tai chua
     */
    boolean existsByUsername(String username);
    
    /**
     * Kiem tra email da ton tai chua
     */
    boolean existsByEmail(String email);
}
