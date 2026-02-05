package com.app.dangdoanhtoai2280603283.service;

import com.app.dangdoanhtoai2280603283.exception.BadRequestException;
import com.app.dangdoanhtoai2280603283.exception.ResourceNotFoundException;
import com.app.dangdoanhtoai2280603283.model.Role;
import com.app.dangdoanhtoai2280603283.model.User;
import com.app.dangdoanhtoai2280603283.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service xu ly User Management
 * - Lay danh sach user
 * - Cap nhat role user
 * - Khoai/Mo khoai user
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * Lay danh sach tat ca user (phan trang)
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Lay danh sach tat ca user (khong phan trang)
     */
    public List<User> getAllUsersList() {
        return userRepository.findAll();
    }

    /**
     * Lay user theo ID
     */
    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Cap nhat role user
     */
    public User updateUserRole(String userId, Role newRole) {
        User user = getUserById(userId);
        
        // Kiểm tra nếu user muốn đổi role của chính mình
        // Có thể thêm logic kiểm tra ở đây nếu cần
        
        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * Khoai user (disable)
     */
    public User disableUser(String userId) {
        User user = getUserById(userId);
        user.setEnabled(false);
        return userRepository.save(user);
    }

    /**
     * Mo khoai user (enable)
     */
    public User enableUser(String userId) {
        User user = getUserById(userId);
        user.setEnabled(true);
        return userRepository.save(user);
    }

    /**
     * Toggle trạng thái active/inactive của user
     */
    public User toggleUserStatus(String userId) {
        User user = getUserById(userId);
        user.setEnabled(!user.getEnabled());
        return userRepository.save(user);
    }

    /**
     * Xóa user
     */
    public void deleteUser(String userId) {
        User user = getUserById(userId);
        
        // Kiểm tra nếu user muốn xóa chính mình
        // Có thể thêm logic kiểm tra ở đây nếu cần
        
        userRepository.delete(user);
    }

    /**
     * Đếm số lượng user theo role
     */
    public long countByRole(Role role) {
        return userRepository.findAll().stream()
                .filter(user -> user.getRole() == role)
                .count();
    }
}
