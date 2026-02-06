package com.app.dangdoanhtoai2280603283.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
 * OAUTH2 CONTROLLER - XU LY DANG NHAP GOOGLE
 */
@Slf4j
@Controller
@RequestMapping("/auth")
public class OAuth2Controller {

    /**
     * Redirect den Spring Security OAuth2 endpoint
     * Frontend se goi endpoint nay khi user click "Dang nhap bang Google"
     */
    @GetMapping("/google")
    public void redirectToGoogleOAuth2(HttpServletResponse response) throws IOException {
        // Redirect den endpoint mac dinh cua Spring Security OAuth2
        response.sendRedirect("/oauth2/authorization/google");
    }
}
