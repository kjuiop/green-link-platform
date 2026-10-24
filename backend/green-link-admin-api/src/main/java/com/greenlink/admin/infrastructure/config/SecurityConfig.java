package com.greenlink.admin.infrastructure.config;

import com.greenlink.admin.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/companies/search").permitAll()
                .requestMatchers("/api/vendor/**").hasRole("VENDOR")
                .requestMatchers("/api/ordering/**").hasRole("ORDERING")
                .requestMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ORDERING")
                .requestMatchers(HttpMethod.GET, "/api/menus/by-role").authenticated()
                .requestMatchers("/api/menus", "/api/menus/**").hasAnyRole("SUPER_ADMIN", "ORDERING")
                .requestMatchers("/api/vendors", "/api/vendors/**").hasAnyRole("SUPER_ADMIN", "ORDERING")
                .requestMatchers("/api/photos", "/api/photos/**").hasAnyRole("SUPER_ADMIN", "ORDERING")
                .requestMatchers(HttpMethod.GET, "/api/products", "/api/products/**").authenticated()
                .requestMatchers("/api/products", "/api/products/**").hasAnyRole("SUPER_ADMIN", "ORDERING")
                .requestMatchers("/api/orders", "/api/orders/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
