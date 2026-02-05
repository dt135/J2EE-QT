package com.app.dangdoanhtoai2280603283.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CartItem - Mot item trong gio hang
 * Luu bookId va so luong
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    private String bookId;
    
    @Builder.Default
    private Integer quantity = 1;
}
