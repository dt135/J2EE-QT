package com.app.dangdoanhtoai2280603283.service;

import com.app.dangdoanhtoai2280603283.dto.CheckoutResponse;
import com.app.dangdoanhtoai2280603283.exception.BadRequestException;
import com.app.dangdoanhtoai2280603283.exception.ResourceNotFoundException;
import com.app.dangdoanhtoai2280603283.model.*;
import com.app.dangdoanhtoai2280603283.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service xu ly Invoice/Checkout (BAI 5)
 */
@Service
@RequiredArgsConstructor
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    /**
     * Thanh toan gio hang (Checkout)
     * POST /checkout
     * 
     * LUONG XU LY:
     * 1. Lay gio hang cua user
     * 2. Kiem tra gio hang khong trong
     * 3. Tinh tong tien
     * 4. Tao Invoice
     * 5. Tao Items
     * 6. Xoa gio hang
     */
    @Transactional
    public CheckoutResponse checkout(String userId) {
        // Lay cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BadRequestException("Gio hang trong"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Gio hang trong. Vui long them san pham truoc khi thanh toan.");
        }

        // Lay user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Tinh tong tien va tao danh sach items
        double totalAmount = 0;
        List<CheckoutResponse.ItemResponse> itemResponses = new ArrayList<>();
        List<Item> items = new ArrayList<>();
        int totalQuantity = 0;

        for (CartItem cartItem : cart.getItems()) {
            Book book = bookRepository.findById(cartItem.getBookId()).orElse(null);
            if (book != null) {
                double subtotal = book.getPrice() * cartItem.getQuantity();
                totalAmount += subtotal;
                totalQuantity += cartItem.getQuantity();

                itemResponses.add(CheckoutResponse.ItemResponse.builder()
                        .bookId(book.getId())
                        .bookTitle(book.getTitle())
                        .price(book.getPrice())
                        .quantity(cartItem.getQuantity())
                        .subtotal(subtotal)
                        .build());

                items.add(Item.builder()
                        .book(book)
                        .bookId(book.getId())
                        .price(book.getPrice())
                        .quantity(cartItem.getQuantity())
                        .build());
            }
        }

        if (items.isEmpty()) {
            throw new BadRequestException("Khong co san pham hop le trong gio hang");
        }

        // Tao Invoice
        Invoice invoice = Invoice.builder()
                .userId(userId)
                .user(user)
                .totalAmount(totalAmount)
                .build();

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Tao Items
        for (Item item : items) {
            item.setInvoiceId(savedInvoice.getId());
            itemRepository.save(item);
        }

        // Xoa gio hang
        cart.clear();
        cartRepository.save(cart);

        // Build response
        return CheckoutResponse.builder()
                .invoice(savedInvoice)
                .items(itemResponses)
                .summary(CheckoutResponse.CheckoutSummary.builder()
                        .totalItems(items.size())
                        .totalQuantity(totalQuantity)
                        .totalAmount(totalAmount)
                        .build())
                .build();
    }

    /**
     * Lay lich su hoa don cua user
     * GET /invoices
     */
    public Page<Invoice> getInvoicesByUser(String userId, Pageable pageable) {
        return invoiceRepository.findByUserId(userId, pageable);
    }

    /**
     * Lay chi tiet hoa don
     * GET /invoices/:id
     */
    public Invoice getInvoiceById(String id, String userId, boolean isAdmin) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        // Kiem tra quyen (chi xem hoa don cua minh, tru ADMIN)
        if (!isAdmin && !invoice.getUserId().equals(userId)) {
            throw new BadRequestException("Ban khong co quyen xem hoa don nay");
        }

        return invoice;
    }

    /**
     * Lay chi tiet items cua hoa don
     */
    public List<Item> getInvoiceItems(String invoiceId) {
        return itemRepository.findByInvoiceId(invoiceId);
    }

    /**
     * Lay tat ca hoa don (ADMIN)
     * GET /invoices/all
     */
    public Page<Invoice> getAllInvoices(Pageable pageable) {
        return invoiceRepository.findAll(pageable);
    }
}
