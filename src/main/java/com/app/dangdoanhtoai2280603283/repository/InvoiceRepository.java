package com.app.dangdoanhtoai2280603283.repository;

import com.app.dangdoanhtoai2280603283.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Invoice Entity
 */
@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {
    
    /**
     * Tim tat ca invoice cua user
     */
    List<Invoice> findByUserIdOrderByCreatedAtDesc(String userId);
    
    /**
     * Tim invoice cua user voi phan trang
     */
    Page<Invoice> findByUserId(String userId, Pageable pageable);
}
