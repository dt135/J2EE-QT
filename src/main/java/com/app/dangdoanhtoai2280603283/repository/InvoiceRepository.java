package com.app.dangdoanhtoai2280603283.repository;

import com.app.dangdoanhtoai2280603283.model.Invoice;
import com.app.dangdoanhtoai2280603283.model.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    /**
     * ADMIN: Tim invoice theo trang thai
     */
    Page<Invoice> findByStatus(OrderStatus status, Pageable pageable);

    /**
     * ADMIN: Tim invoice theo khoang thoi gian
     */
    Page<Invoice> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * ADMIN: Tim invoice theo trang thai va khoang thoi gian
     */
    Page<Invoice> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * ADMIN: Thong ke doanh thu theo thang (trang thai COMPLETED)
     */
    List<Invoice> findByStatusAndCreatedAtBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
