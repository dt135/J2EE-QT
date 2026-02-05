package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.*;
import com.app.dangdoanhtoai2280603283.model.User;
import com.app.dangdoanhtoai2280603283.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Controller xu ly Authentication (BAI 6)
 * - POST /auth/register: Dang ky
 * - POST /auth/login: Dang nhap
 * - GET /auth/me: Lay thong tin user hien tai
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * DANG KY TAI KHOAN
     * POST /auth/register
     * 
     * Request Body: { username, email, password, role? }
     * Response: { success, message, data: { user, token } }
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Dang ky thanh cong", response));
    }

    /**
     * DANG NHAP
     * POST /auth/login
     * 
     * Request Body: { username, password }
     * Response: { success, message, data: { user, token } }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Dang nhap thanh cong", response));
    }

    /**
     * LAY THONG TIN USER HIEN TAI
     * GET /auth/me
     * 
     * Yeu cau: Bearer Token trong header Authorization
     * Response: { success, data: { user } }
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(user));
    }
}
