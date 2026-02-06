package com.app.dangdoanhtoai2280603283.security;

import com.app.dangdoanhtoai2280603283.service.OAuth2Service;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * OAUTH2 SUCCESS HANDLER
 */
@Slf4j
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuth2Service oAuth2Service;

    @Value("${frontend.url:http://localhost:8082/frontend/pages}")
    private String frontendUrl;

    public OAuth2SuccessHandler(OAuth2Service oAuth2Service) {
        this.oAuth2Service = oAuth2Service;
    }

    /**
     * Sau khi dang nhap Google thanh cong
     * - Lay thong tin OAuth2User
     * - Goi OAuth2Service de tao/cap nhat user trong database
     * - Sinh JWT token
     * - Redirect ve frontend voi token trong URL
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Success Handler - Processing successful authentication");

        try {
            // Lay OAuth2User tu Authentication
            OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

            // Goi OAuth2Service de xu ly user
            Map<String, Object> result = oAuth2Service.processOAuth2User(oAuth2User);

            String userId = (String) result.get("userId");
            String email = (String) result.get("email");
            String username = (String) result.get("username");
            String role = (String) result.get("role");
            String provider = (String) result.get("provider");

            // Sinh JWT token
            String token = jwtTokenProvider.generateToken(username);

            // Build redirect URL voi token trong query param
            String redirectUrl = frontendUrl + "/index.html?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8)
                    + "&userId=" + URLEncoder.encode(userId, StandardCharsets.UTF_8)
                    + "&email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                    + "&username=" + URLEncoder.encode(username, StandardCharsets.UTF_8)
                    + "&role=" + role
                    + "&provider=" + provider;

            log.info("OAuth2 Success - Redirecting to frontend with token");
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 Success Handler Error: {}", e.getMessage());
            // Redirect ve trang login voi error
            String errorUrl = frontendUrl + "/login.html?error=oauth2_error&message="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
        }
    }

    private final JwtTokenProvider jwtTokenProvider = new JwtTokenProvider();
}
