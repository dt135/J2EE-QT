package com.app.dangdoanhtoai2280603283.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.core.web.OAuth2AuthorizationRequestResolver;
import org.springframework.util.SerializationUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

import java.util.Base64;
import java.util.Optional;

/**
 * COOKIE-BASED OAUTH2 AUTHORIZATION REQUEST REPOSITORY
 */
public class CookieOAuth2AuthorizationRequestRepository
        implements OAuth2AuthorizationRequestResolver {

    public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
    public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
    private static final int cookieExpireSeconds = 180;

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        return resolve(request, "");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String registrationId) {
        return loadAuthorizationRequest(request);
    }

    @Override
    public OAuth2AuthorizationRequest resolveAuthorizationRequest(HttpServletRequest request) {
        return resolve(request);
    }

    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        Cookie cookie = WebUtils.getCookie(request, OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (cookie == null) {
            return null;
        }
        return deserialize(cookie.getValue());
    }

    public void saveAuthorizationRequest(
            OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (authorizationRequest == null) {
            removeAuthorizationRequestCookies(request, response);
            return;
        }

        Cookie cookie = new Cookie(OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, serialize(authorizationRequest));
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(cookieExpireSeconds);

        String redirectUriAfterLogin = request.getParameter(REDIRECT_URI_PARAM_COOKIE_NAME);
        if (StringUtils.hasText(redirectUriAfterLogin)) {
            Cookie redirectCookie = new Cookie(REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin);
            redirectCookie.setPath("/");
            redirectCookie.setMaxAge(cookieExpireSeconds);
            response.addCookie(redirectCookie);
        }

        response.addCookie(cookie);
    }

    public OAuth2AuthorizationRequest removeAuthorizationRequest(
            HttpServletRequest request,
            HttpServletResponse response) {

        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        if (authorizationRequest != null) {
            removeAuthorizationRequestCookies(request, response);
        }

        return authorizationRequest;
    }

    public void removeAuthorizationRequestCookies(
            HttpServletRequest request,
            HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                String cookieName = cookie.getName();
                if (OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME.equals(cookieName)
                        || REDIRECT_URI_PARAM_COOKIE_NAME.equals(cookieName)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    /**
     * Serialize OAuth2AuthorizationRequest thanh String (Base64)
     */
    private String serialize(OAuth2AuthorizationRequest object) {
        try {
            return Base64.getUrlEncoder()
                    .encodeToString(SerializationUtils.serialize(object));
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize OAuth2AuthorizationRequest", e);
        }
    }

    /**
     * Deserialize String (Base64) thanh OAuth2AuthorizationRequest
     */
    private OAuth2AuthorizationRequest deserialize(String serialized) {
        try {
            byte[] bytes = Base64.getUrlDecoder().decode(serialized);
            return (OAuth2AuthorizationRequest) SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            return null;
        }
    }
}
