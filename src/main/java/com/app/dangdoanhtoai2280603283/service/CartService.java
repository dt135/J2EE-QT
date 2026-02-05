package com.app.dangdoanhtoai2280603283.service;

import com.app.dangdoanhtoai2280603283.dto.CartRequest;
import com.app.dangdoanhtoai2280603283.dto.CartResponse;
import com.app.dangdoanhtoai2280603283.exception.BadRequestException;
import com.app.dangdoanhtoai2280603283.exception.ResourceNotFoundException;
import com.app.dangdoanhtoai2280603283.model.Book;
import com.app.dangdoanhtoai2280603283.model.Cart;
import com.app.dangdoanhtoai2280603283.model.CartItem;
import com.app.dangdoanhtoai2280603283.repository.BookRepository;
import com.app.dangdoanhtoai2280603283.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service xu ly Cart (BAI 5)
 */
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final BookRepository bookRepository;

    /**
     * Lay gio hang cua user
     * GET /cart
     */
    public CartResponse getCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        return buildCartResponse(cart);
    }

    /**
     * Them sach vao gio hang
     * POST /cart/add
     */
    public CartResponse addToCart(String userId, CartRequest request) {
        // Kiem tra sach ton tai
        bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book", "id", request.getBookId()));

        Cart cart = getOrCreateCart(userId);
        cart.addItem(request.getBookId(), request.getQuantity());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    /**
     * Cap nhat so luong sach trong gio
     * PUT /cart/update
     */
    public CartResponse updateCartItem(String userId, CartRequest request) {
        Cart cart = getOrCreateCart(userId);

        // Kiem tra item co trong gio khong
        boolean itemExists = cart.getItems().stream()
                .anyMatch(item -> item.getBookId().equals(request.getBookId()));

        if (!itemExists) {
            throw new BadRequestException("Sach khong co trong gio hang");
        }

        cart.updateItemQuantity(request.getBookId(), request.getQuantity());
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    /**
     * Xoa sach khoi gio hang
     * DELETE /cart/remove
     */
    public CartResponse removeFromCart(String userId, String bookId) {
        Cart cart = getOrCreateCart(userId);
        cart.removeItem(bookId);
        cartRepository.save(cart);

        return buildCartResponse(cart);
    }

    /**
     * Xoa toan bo gio hang
     * DELETE /cart/clear
     */
    public void clearCart(String userId) {
        Cart cart = getOrCreateCart(userId);
        cart.clear();
        cartRepository.save(cart);
    }

    /**
     * Lay hoac tao moi cart cho user
     */
    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .items(new ArrayList<>())
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Build CartResponse tu Cart entity
     */
    private CartResponse buildCartResponse(Cart cart) {
        List<CartResponse.CartItemResponse> itemResponses = new ArrayList<>();
        double totalAmount = 0;

        for (CartItem item : cart.getItems()) {
            Book book = bookRepository.findById(item.getBookId()).orElse(null);
            if (book != null) {
                double subtotal = book.getPrice() * item.getQuantity();
                totalAmount += subtotal;

                itemResponses.add(CartResponse.CartItemResponse.builder()
                        .book(book)
                        .quantity(item.getQuantity())
                        .subtotal(subtotal)
                        .build());
            }
        }

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUserId())
                .items(itemResponses)
                .totalAmount(totalAmount)
                .itemCount(itemResponses.size())
                .build();
    }
}
