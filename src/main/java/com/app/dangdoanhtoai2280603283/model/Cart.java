package com.app.dangdoanhtoai2280603283.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Cart Entity - Gio hang nguoi dung
 * Moi user chi co 1 cart (unique userId)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "carts")
public class Cart {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    @Builder.Default
    private List<CartItem> items = new ArrayList<>();

    @LastModifiedDate
    private LocalDateTime updatedAt;

    /**
     * Them sach vao gio hang
     */
    public void addItem(String bookId, int quantity) {
        // Tim xem sach da co trong gio chua
        for (CartItem item : items) {
            if (item.getBookId().equals(bookId)) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        // Neu chua co, them moi
        items.add(CartItem.builder()
                .bookId(bookId)
                .quantity(quantity)
                .build());
    }

    /**
     * Cap nhat so luong
     */
    public void updateItemQuantity(String bookId, int quantity) {
        for (CartItem item : items) {
            if (item.getBookId().equals(bookId)) {
                if (quantity <= 0) {
                    removeItem(bookId);
                } else {
                    item.setQuantity(quantity);
                }
                return;
            }
        }
    }

    /**
     * Xoa sach khoi gio
     */
    public void removeItem(String bookId) {
        items.removeIf(item -> item.getBookId().equals(bookId));
    }

    /**
     * Xoa toan bo gio hang
     */
    public void clear() {
        items.clear();
    }
}
