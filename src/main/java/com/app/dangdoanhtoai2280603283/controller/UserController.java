package com.app.dangdoanhtoai2280603283.controller;

import com.app.dangdoanhtoai2280603283.dto.ApiResponse;
import com.app.dangdoanhtoai2280603283.dto.PageResponse;
import com.app.dangdoanhtoai2280603283.model.Role;
import com.app.dangdoanhtoai2280603283.model.User;
import com.app.dangdoanhtoai2280603283.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller xu ly User Management (ADMIN only)
 * - GET /users: Lay danh sach user
 * - GET /users/:id: Lay chi tiet user
 * - PUT /users/:id/role: Cap nhat role user
 * - PUT /users/:id/status: Khoai/Mo khoai user
 * - DELETE /users/:id: Xoa user
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * LAY DANH SACH USER (ADMIN only)
     * GET /users?page=0&limit=10
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<List<User>>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        
        Pageable pageable = PageRequest.of(page, limit);
        Page<User> userPage = userService.getAllUsers(pageable);
        
        PageResponse<List<User>> pageResponse = PageResponse.<List<User>>builder()
                .content(userPage.getContent())
                .page(page)
                .limit(limit)
                .total(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(pageResponse));
    }

    /**
     * LAY CHI TIET USER (ADMIN only)
     * GET /users/:id
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable String id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * CAP NHAT ROLE USER (ADMIN only)
     * PUT /users/:id/role
     * 
     * Request Body: { role: "ADMIN" | "USER" }
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> updateUserRole(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {
        
        String roleStr = request.get("role");
        if (roleStr == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Role is required"));
        }
        
        Role newRole;
        try {
            newRole = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid role. Must be ADMIN or USER"));
        }
        
        User user = userService.updateUserRole(id, newRole);
        return ResponseEntity.ok(ApiResponse.success("Cap nhat role thanh cong", user));
    }

    /**
     * TOGGLE TRANG THAI USER (Khoai/Mo khoai) (ADMIN only)
     * PUT /users/:id/status
     * 
     * Request Body: { enabled: true | false }
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<User>> toggleUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean enabled = request.get("enabled");
        if (enabled == null) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Enabled status is required"));
        }
        
        User user;
        if (enabled) {
            user = userService.enableUser(id);
        } else {
            user = userService.disableUser(id);
        }
        
        String message = enabled ? "Da mo khoai user" : "Da khoai user";
        return ResponseEntity.ok(ApiResponse.success(message, user));
    }

    /**
     * XOA USER (ADMIN only)
     * DELETE /users/:id
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Xoa user thanh cong", null));
    }

    /**
     * THONG KE USERS (ADMIN only)
     * GET /users/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getUserStats() {
        long totalUsers = userService.getAllUsersList().size();
        long adminCount = userService.countByRole(Role.ADMIN);
        long userCount = userService.countByRole(Role.USER);
        long activeCount = userService.getAllUsersList().stream()
                .filter(u -> u.getEnabled() != null && u.getEnabled())
                .count();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", totalUsers);
        stats.put("adminCount", adminCount);
        stats.put("userCount", userCount);
        stats.put("activeCount", activeCount);
        stats.put("inactiveCount", totalUsers - activeCount);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
