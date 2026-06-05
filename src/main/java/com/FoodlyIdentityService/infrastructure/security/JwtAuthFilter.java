package com.FoodlyIdentityService.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    public JwtAuthFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Rutas publicas (Auth y Swagger)
        return path.startsWith("/api/auth/") || 
               path.startsWith("/openapi") || 
               path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            abortWithUnauthorized(response, "Token de autenticación requerido");
            return;
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            abortWithUnauthorized(response, "Token JWT vacío");
            return;
        }

        if (!jwtProvider.validateToken(token)) {
            abortWithUnauthorized(response, "Token JWT inválido o expirado");
            return;
        }

        String userId = jwtProvider.getUserId(token);
        String email = jwtProvider.getEmail(token);
        List<String> roles = jwtProvider.getRoles(token);

        request.setAttribute("userId", userId);
        request.setAttribute("email", email);
        request.setAttribute("roles", roles);

        filterChain.doFilter(request, response);
    }

    private void abortWithUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
