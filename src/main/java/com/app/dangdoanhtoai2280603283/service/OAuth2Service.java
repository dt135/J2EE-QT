package com.app.dangdoanhtoai2280603283.service;

import com.app.dangdoanhtoai2280603283.model.Role;
import com.app.dangdoanhtoai2280603283.model.User;
import com.app.dangdoanhtoai2280603283.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * OAUTH2 SERVICE - XU LY LOGIC DANG NHAP GOOGLE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UserRepository userRepository;

    /**
     * Xu ly user tu Google OAuth2
     * @param oAuth2User - Thong tin user tu Google
     * @return Map chua thong tin user da tao/cap nhat
     */
    public Map<String, Object> processOAuth2User(OAuth2User oAuth2User) {
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String picture = oAuth2User.getAttribute("picture");

        log.info("OAuth2 Login - Google ID: {}, Email: {}, Name: {}", googleId, email, name);

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            // User da ton tai
            user = userOptional.get();

            // Cap nhat thong tin neu can
            if ("GOOGLE".equals(user.getProvider())) {
                // User da lien ket Google - cap nhat thong tin
                user.setUsername(name != null ? name : email);
                if (picture != null) {
                    // TODO: Them avatarUrl field neu can
                }
                user = userRepository.save(user);
                log.info("OAuth2 Login - Updated existing Google user: {}", email);
            } else {
                // User dang ky thuong (LOCAL) - lien ket Google
                user.setGoogleId(googleId);
                user.setProvider("GOOGLE");
                if (picture != null) {
                    // TODO: Them avatarUrl field neu can
                }
                user = userRepository.save(user);
                log.info("OAuth2 Login - Linked LOCAL account to Google: {}", email);
            }
        } else {
            // User moi - tao tai khoan moi
            user = User.builder()
                    .username(name != null ? name : email)
                    .email(email)
                    .password(null) // Khong can password cho OAuth2 user
                    .role(Role.USER) // Google user luon la USER, khong co ADMIN
                    .enabled(true)
                    .provider("GOOGLE")
                    .googleId(googleId)
                    .build();

            user = userRepository.save(user);
            log.info("OAuth2 Login - Created new Google user: {}", email);
        }

        return Map.of(
                "userId", user.getId(),
                "email", user.getEmail(),
                "username", user.getUsername(),
                "role", user.getRole().name(),
                "provider", user.getProvider()
        );
    }
}
