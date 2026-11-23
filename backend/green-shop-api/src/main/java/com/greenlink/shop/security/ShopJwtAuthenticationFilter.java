package com.greenlink.shop.security;

import com.greenlink.shopcore.domain.auth.TokenScope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ShopJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER = "Bearer ";

    private final ShopJwtProvider jwtProvider;

    public ShopJwtAuthenticationFilter(ShopJwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER)) {
            jwtProvider.parse(header.substring(BEARER.length())).ifPresent(principal -> {
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(
                    principal.scope() == TokenScope.FULL ? "SCOPE_FULL" : "SCOPE_LIMITED"));
                if (principal.scope() == TokenScope.FULL) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + principal.role().name()));
                }
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(principal, null, authorities));
            });
        }
        filterChain.doFilter(request, response);
    }
}
