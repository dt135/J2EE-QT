package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.ApiResponse;
import com.app.dangdoanhtoai2280603283.dto.CartRequest;
import com.app.dangdoanhtoai2280603283.dto.CartResponse;
import com.app.dangdoanhtoai2280603283.model.User;
import com.app.dangdoanhtoai2280603283.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xu ly Cart (BAI 5)
 * - GET /cart: Lay gio hang
 * - POST /cart/add: Them sach vao gio
 * - PUT /cart/update: Cap nhat so luong
 * - DELETE /cart/remove: Xoa sach khoi gio
 * - DELETE /cart/clear: Xoa toan bo gio
 *
 * CHI DUOC PHEP CHO USER (KHONG PHAI ADMIN)
 */
@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    /**
     * LAY GIO HANG CUA USER HIEN TAI
     * GET /cart
     * Chi USER duoc phep su dung cart, ADMIN khong
     */
    @GetMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        CartResponse cart = cartService.getCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success(cart));
    }

    /**
     * THEM SACH VAO GIO HANG
     * POST /cart/add
     *
     * Body: { bookId, quantity? }
     * Chi USER duoc phep them vao gio
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CartResponse>> addToCart(
            Authentication authentication,
            @Valid @RequestBody CartRequest request) {
        User user = (User) authentication.getPrincipal();
        CartResponse cart = cartService.addToCart(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Da them vao gio hang", cart));
    }

    /**
     * CAP NHAT SO LUONG SACH
     * PUT /cart/update
     *
     * Body: { bookId, quantity }
     * Chi USER duoc phep cap nhat gio
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CartResponse>> updateCartItem(
            Authentication authentication,
            @Valid @RequestBody CartRequest request) {
        User user = (User) authentication.getPrincipal();
        CartResponse cart = cartService.updateCartItem(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat thanh cong", cart));
    }

    /**
     * XOA SACH KHOI GIO HANG
     * DELETE /cart/remove
     *
     * Body: { bookId }
     * Chi USER duoc phep xoa khoi gio
     */
    @DeleteMapping("/remove")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<CartResponse>> removeFromCart(
            Authentication authentication,
            @RequestBody CartRequest request) {
        User user = (User) authentication.getPrincipal();
        CartResponse cart = cartService.removeFromCart(user.getId(), request.getBookId());
        return ResponseEntity.ok(ApiResponse.success("Da xoa khoi gio hang", cart));
    }

    /**
     * XOA TOAN BO GIO HANG
     * DELETE /cart/clear
     * Chi USER duoc phep xoa gio
     */
    @DeleteMapping("/clear")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        cartService.clearCart(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Da xoa toan bo gio hang", null));
    }
}
