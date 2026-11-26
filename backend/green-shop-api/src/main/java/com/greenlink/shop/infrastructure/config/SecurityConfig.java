package com.greenlink.shop.infrastructure.config;

import com.greenlink.shop.security.ShopJwtAuthenticationFilter;
import com.greenlink.shop.security.ShopJwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.nio.charset.StandardCharsets;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String UNAUTHORIZED_BODY =
        "{\"code\":\"SHOP_AUTH_401\",\"message\":\"인증이 필요합니다.\"}";
    private static final String FORBIDDEN_BODY =
        "{\"code\":\"SHOP_AUTH_403\",\"message\":\"접근 권한이 없습니다.\"}";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, ShopJwtProvider jwtProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(new ShopJwtAuthenticationFilter(jwtProvider), UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> writeJson(res, HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORIZED_BODY))
                .accessDeniedHandler((req, res, e) -> writeJson(res, HttpServletResponse.SC_FORBIDDEN, FORBIDDEN_BODY)))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**", "/api/ping").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/signup", "/api/auth/password-setup").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/auth/business-numbers/*/exists").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/refresh").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/me", "/api/access-requests/brands").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/access-requests").authenticated()
                .requestMatchers("/api/admin/**").hasAuthority("ROLE_SUPER_ADMIN")
                .anyRequest().hasAuthority("SCOPE_FULL")
            );
        return http.build();
    }

    // 기본 사용자 자동 생성(콘솔에 임시 비밀번호 출력)을 막기 위한 빈 저장소
    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        return new InMemoryUserDetailsManager();
    }

    private static void writeJson(HttpServletResponse res, int status, String body) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType("application/json;charset=UTF-8");
        res.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    }
}
