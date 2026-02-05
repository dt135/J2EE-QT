package com.app.dangdoanhtoai2280603283.service;

import com.app.dangdoanhtoai2280603283.dto.AdminOrderResponse;
import com.app.dangdoanhtoai2280603283.dto.CheckoutResponse;
import com.app.dangdoanhtoai2280603283.dto.OrderHistoryResponse;
import com.app.dangdoanhtoai2280603283.dto.RevenueResponse;
import com.app.dangdoanhtoai2280603283.exception.BadRequestException;
import com.app.dangdoanhtoai2280603283.exception.ResourceNotFoundException;
import com.app.dangdoanhtoai2280603283.model.*;
import com.app.dangdoanhtoai2280603283.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
                .status(OrderStatus.PENDING)
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
     * Lay lich su don hang cho order history (USER)
     * GET /orders/history
     */
    public OrderHistoryResponse getOrderHistory(String userId, int page, int limit) {
        Pageable pageable = PageRequest.of(page, limit, Sort.by("createdAt").descending());
        Page<Invoice> invoicePage = invoiceRepository.findByUserId(userId, pageable);

        List<OrderHistoryResponse.OrderSummary> orders = invoicePage.getContent().stream()
                .map(invoice -> {
                    List<Item> items = itemRepository.findByInvoiceId(invoice.getId());
                    return OrderHistoryResponse.OrderSummary.builder()
                            .orderId(invoice.getId())
                            .orderNumber(generateOrderNumber(invoice.getId(), invoice.getCreatedAt()))
                            .createdAt(invoice.getCreatedAt())
                            .totalAmount(invoice.getTotalAmount())
                            .status(invoice.getStatus() != null ? invoice.getStatus().name() : "PENDING")
                            .itemCount(items.size())
                            .build();
                })
                .toList();

        return OrderHistoryResponse.builder()
                .orders(orders)
                .total((int) invoicePage.getTotalElements())
                .page(page)
                .limit(limit)
                .totalPages(invoicePage.getTotalPages())
                .build();
    }

    /**
     * Lay chi tiet don hang
     * GET /orders/:id
     */
    public OrderHistoryResponse.OrderDetail getOrderDetail(String orderId, String userId) {
        Invoice invoice = getInvoiceById(orderId, userId, false);
        List<Item> items = itemRepository.findByInvoiceId(orderId);

        List<OrderHistoryResponse.OrderItemResponse> itemResponses = items.stream()
                .map(item -> {
                    Book book = bookRepository.findById(item.getBookId()).orElse(null);
                    return OrderHistoryResponse.OrderItemResponse.builder()
                            .bookId(item.getBookId())
                            .bookTitle(book != null ? book.getTitle() : "Unknown")
                            .bookAuthor(book != null ? book.getAuthor() : "Unknown")
                            .price(item.getPrice())
                            .quantity(item.getQuantity())
                            .subtotal(item.getPrice() * item.getQuantity())
                            .build();
                })
                .toList();

        return OrderHistoryResponse.OrderDetail.builder()
                .orderId(invoice.getId())
                .orderNumber(generateOrderNumber(invoice.getId(), invoice.getCreatedAt()))
                .createdAt(invoice.getCreatedAt())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus() != null ? invoice.getStatus().name() : "PENDING")
                .items(itemResponses)
                .build();
    }

    /**
     * Tao ma don hang tu ID va thoi gian
     */
    private String generateOrderNumber(String id, LocalDateTime createdAt) {
        if (createdAt == null) return id;
        return "ORD-" + createdAt.getYear() +
                String.format("%02d", createdAt.getMonthValue()) +
                String.format("%02d", createdAt.getDayOfMonth()) +
                "-" + id.substring(0, 8);
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

    // ===== ADMIN METHODS =====

    /**
     * Lay danh sach don hang cho ADMIN voi loc theo trang thai va thoi gian
     * GET /admin/orders
     */
    public Page<AdminOrderResponse.InvoiceResponse> getAdminOrders(
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        Page<Invoice> invoicePage;

        if (status != null && fromDate != null && toDate != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            invoicePage = invoiceRepository.findByStatusAndCreatedAtBetween(orderStatus, fromDate, toDate, pageable);
        } else if (status != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            invoicePage = invoiceRepository.findByStatus(orderStatus, pageable);
        } else if (fromDate != null && toDate != null) {
            invoicePage = invoiceRepository.findByCreatedAtBetween(fromDate, toDate, pageable);
        } else {
            invoicePage = invoiceRepository.findAll(pageable);
        }

        return invoicePage.map(invoice -> {
            List<Item> items = itemRepository.findByInvoiceId(invoice.getId());
            return AdminOrderResponse.InvoiceResponse.builder()
                    .orderId(invoice.getId())
                    .orderNumber(generateOrderNumber(invoice.getId(), invoice.getCreatedAt()))
                    .username(invoice.getUser() != null ? invoice.getUser().getUsername() : "Unknown")
                    .email(invoice.getUser() != null ? invoice.getUser().getEmail() : "Unknown")
                    .createdAt(invoice.getCreatedAt())
                    .totalAmount(invoice.getTotalAmount())
                    .status(invoice.getStatus() != null ? invoice.getStatus().name() : "PENDING")
                    .itemCount(items.size())
                    .build();
        });
    }

    /**
     * Lay chi tiet don hang cho ADMIN
     * GET /admin/orders/:orderId
     */
    public AdminOrderResponse.OrderDetailResponse getAdminOrderDetail(String orderId) {
        Invoice invoice = invoiceRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", orderId));

        List<Item> items = itemRepository.findByInvoiceId(orderId);

        List<AdminOrderResponse.OrderItemResponse> itemResponses = items.stream()
                .map(item -> {
                    Book book = bookRepository.findById(item.getBookId()).orElse(null);
                    return AdminOrderResponse.OrderItemResponse.builder()
                            .bookId(item.getBookId())
                            .bookTitle(book != null ? book.getTitle() : "Unknown")
                            .bookAuthor(book != null ? book.getAuthor() : "Unknown")
                            .price(item.getPrice())
                            .quantity(item.getQuantity())
                            .subtotal(item.getPrice() * item.getQuantity())
                            .build();
                })
                .collect(Collectors.toList());

        return AdminOrderResponse.OrderDetailResponse.builder()
                .orderId(invoice.getId())
                .orderNumber(generateOrderNumber(invoice.getId(), invoice.getCreatedAt()))
                .username(invoice.getUser() != null ? invoice.getUser().getUsername() : "Unknown")
                .email(invoice.getUser() != null ? invoice.getUser().getEmail() : "Unknown")
                .createdAt(invoice.getCreatedAt())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus() != null ? invoice.getStatus().name() : "PENDING")
                .items(itemResponses)
                .build();
    }

    /**
     * Cap nhat trang thai don hang
     * PUT /admin/orders/:orderId/status
     */
    public Invoice updateOrderStatus(String orderId, String status) {
        Invoice invoice = invoiceRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", orderId));

        OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
        invoice.setStatus(orderStatus);

        return invoiceRepository.save(invoice);
    }

    /**
     * Lay danh sach don hang de export
     * GET /admin/orders/export
     */
    public List<AdminOrderResponse.InvoiceResponse> getOrdersForExport(
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate) {

        List<Invoice> invoices;

        if (status != null && fromDate != null && toDate != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            invoices = invoiceRepository.findByStatusAndCreatedAtBetween(orderStatus, fromDate, toDate);
        } else if (status != null) {
            OrderStatus orderStatus = OrderStatus.valueOf(status.toUpperCase());
            invoices = invoiceRepository.findByStatus(orderStatus, Pageable.unpaged()).getContent();
        } else if (fromDate != null && toDate != null) {
            invoices = invoiceRepository.findByCreatedAtBetween(fromDate, toDate, Pageable.unpaged()).getContent();
        } else {
            invoices = invoiceRepository.findAll();
        }

        return invoices.stream()
                .map(invoice -> {
                    List<Item> items = itemRepository.findByInvoiceId(invoice.getId());
                    return AdminOrderResponse.InvoiceResponse.builder()
                            .orderId(invoice.getId())
                            .orderNumber(generateOrderNumber(invoice.getId(), invoice.getCreatedAt()))
                            .username(invoice.getUser() != null ? invoice.getUser().getUsername() : "Unknown")
                            .email(invoice.getUser() != null ? invoice.getUser().getEmail() : "Unknown")
                            .createdAt(invoice.getCreatedAt())
                            .totalAmount(invoice.getTotalAmount())
                            .status(invoice.getStatus() != null ? invoice.getStatus().name() : "PENDING")
                            .itemCount(items.size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Thong ke doanh thu theo thang
     * GET /admin/revenue/monthly
     */
    public RevenueResponse getMonthlyRevenue(Integer year) {
        int currentYear = year != null ? year : LocalDateTime.now().getYear();

        List<RevenueResponse.MonthlyRevenue> monthlyRevenues = new ArrayList<>();
        double totalRevenue = 0;

        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};

        for (int month = 1; month <= 12; month++) {
            LocalDateTime startDate = LocalDateTime.of(currentYear, month, 1, 0, 0);
            LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

            List<Invoice> invoices = invoiceRepository.findByStatusAndCreatedAtBetween(
                    OrderStatus.COMPLETED, startDate, endDate);

            double monthlyRevenue = invoices.stream()
                    .mapToDouble(Invoice::getTotalAmount)
                    .sum();
            totalRevenue += monthlyRevenue;

            monthlyRevenues.add(RevenueResponse.MonthlyRevenue.builder()
                    .month(month)
                    .monthName(monthNames[month - 1])
                    .revenue(monthlyRevenue)
                    .orderCount(invoices.size())
                    .build());
        }

        return RevenueResponse.builder()
                .year(currentYear)
                .monthlyRevenues(monthlyRevenues)
                .totalRevenue(totalRevenue)
                .build();
    }

    /**
     * User danh dau don hang la da nhan duoc
     * Cap nhat trang thai thanh COMPLETED
     */
    public void markOrderAsReceived(String orderId, String userId) {
        Invoice invoice = invoiceRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", orderId));

        // Kiểm tra đơn hàng có thuộc về user không
        if (!invoice.getUserId().equals(userId)) {
            throw new BadRequestException("Bạn không có quyền thực hiện thao tác này với đơn hàng này");
        }

        // Kiểm tra trạng thái hiện tại
        if (invoice.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Đơn hàng đã bị hủy, không thể xác nhận nhận hàng");
        }

        if (invoice.getStatus() == OrderStatus.COMPLETED) {
            throw new BadRequestException("Đơn hàng đã hoàn thành");
        }

        // Cập nhật trạng thái thành COMPLETED
        invoice.setStatus(OrderStatus.COMPLETED);
        invoiceRepository.save(invoice);
    }
}
