package com.app.dangdoanhtoai2280603283.repository;

import com.app.dangdoanhtoai2280603283.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho Item Entity
 */
@Repository
public interface ItemRepository extends MongoRepository<Item, String> {
    
    /**
     * Tim tat ca items cua 1 invoice
     */
    List<Item> findByInvoiceId(String invoiceId);
    
    /**
     * Xoa tat ca items cua 1 invoice
     */
    void deleteByInvoiceId(String invoiceId);
}
