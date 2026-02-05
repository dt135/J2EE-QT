package com.app.dangdoanhtoai2280603283.service;

import com.app.dangdoanhtoai2280603283.dto.AuthResponse;
import com.app.dangdoanhtoai2280603283.dto.LoginRequest;
import com.app.dangdoanhtoai2280603283.dto.RegisterRequest;
import com.app.dangdoanhtoai2280603283.exception.BadRequestException;
import com.app.dangdoanhtoai2280603283.model.Role;
import com.app.dangdoanhtoai2280603283.model.User;
import com.app.dangdoanhtoai2280603283.repository.UserRepository;
import com.app.dangdoanhtoai2280603283.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service xu ly Authentication (BAI 6)
 * - Dang ky
 * - Dang nhap
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * Dang ky tai khoan moi
     * POST /auth/register
     */
    public AuthResponse register(RegisterRequest request) {
        // Kiem tra username da ton tai chua
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username da duoc su dung");
        }

        // Kiem tra email da ton tai chua
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email da duoc su dung");
        }

        // Tao user moi
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        // Sinh JWT token
        String token = jwtTokenProvider.generateToken(savedUser.getUsername());

        return AuthResponse.builder()
                .id(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .token(token)
                .build();
    }

    /**
     * Dang nhap
     * POST /auth/login
     */
    public AuthResponse login(LoginRequest request) {
        // Xac thuc username va password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Lay user tu authentication
        User user = (User) authentication.getPrincipal();

        // Sinh JWT token
        String token = jwtTokenProvider.generateToken(user.getUsername());

        return AuthResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole())
                .token(token)
                .build();
    }

    /**
     * Lay thong tin user hien tai
     */
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BadRequestException("User khong ton tai"));
    }
}
