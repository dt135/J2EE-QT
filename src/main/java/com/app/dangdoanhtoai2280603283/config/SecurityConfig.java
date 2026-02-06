package com.app.dangdoanhtoai2280603283.config;

import com.app.dangdoanhtoai2280603283.security.CookieOAuth2AuthorizationRequestRepository;
import com.app.dangdoanhtoai2280603283.security.CustomUserDetailsService;
import com.app.dangdoanhtoai2280603283.security.JwtAuthenticationFilter;
import com.app.dangdoanhtoai2280603283.security.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security Configuration
 * Cau hinh Spring Security voi JWT va OAuth2
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomUserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final CookieOAuth2AuthorizationRequestRepository cookieAuthorizationRequestRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Tat CSRF (vi dung JWT)
                .csrf(AbstractHttpConfigurer::disable)

                // Cau hinh CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Cau hinh authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - khong can xac thuc
                        .requestMatchers("/", "/index").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/login/oauth2/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/books/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll()
                        .requestMatchers("/health").permitAll()

                        // Admin only - CRUD sach va danh muc
                        .requestMatchers(HttpMethod.POST, "/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/books/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/categories/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
                        .requestMatchers("/invoices/all").hasRole("ADMIN")

                        // Admin only - User management
                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // Admin only - Order management
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        // Authenticated - can dang nhap
                        // Note: /cart/** va /checkout da co @PreAuthorize trong controller
                        // Nen day chi can authenticated(), role kiem tra trong controller
                        .requestMatchers("/cart/**").authenticated()
                        .requestMatchers("/checkout").authenticated()
                        .requestMatchers("/invoices/**").authenticated()

                        // Tat ca request khac can xac thuc
                        .anyRequest().authenticated()
                )

                // Stateless session (khong luu session)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Authentication provider
                .authenticationProvider(authenticationProvider())

                // JWT filter truoc UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // ===== OAUTH2 LOGIN (DANG NHAP GOOGLE) =====
        http
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestRepository(cookieAuthorizationRequestRepository)
                                .baseUri("/oauth2/authorization")
                        )
                        .redirectionEndpoint(redirect -> redirect
                                .baseUri("/login/oauth2/code/*")
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureUrl("/frontend/pages/login.html?error=oauth2_error")
                );

        // Session chi duoc tao khi can thiet (cho OAuth2)
        http
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation().migrateSession()
                );

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
