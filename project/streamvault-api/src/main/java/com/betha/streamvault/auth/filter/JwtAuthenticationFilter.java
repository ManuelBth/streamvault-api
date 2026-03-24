package com.betha.streamvault.auth.filter;

import com.betha.streamvault.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    public static final String CURRENT_USER_ATTR = "CURRENT_USER_EMAIL";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        
        log.info("JwtAuthenticationFilter executing for path: {}", request.getRequestURI());
        
        String path = request.getRequestURI();
        
        if (isPublicPath(path)) {
            log.info("Public path, skipping auth");
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(AUTH_HEADER);
        log.info("Auth header: {}", authHeader != null ? "present: '" + authHeader + "'" : "null");
        log.info("Checking if starts with '{}': {}", BEARER_PREFIX, authHeader != null ? authHeader.startsWith(BEARER_PREFIX) : "N/A");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            log.info("No Bearer token found or header doesn't start with Bearer");
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());
        log.info("Token extracted (length {}), validating...", token.length());

        try {
            if (jwtService.isTokenValid(token)) {
                String email = jwtService.getEmailFromToken(token);
                String role = jwtService.getRoleFromToken(token);
                log.info("Token valid! Email: {}, Role: {}", email, role);

                if (email != null && role != null) {
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority(role))
                        );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    request.setAttribute(JwtAuthenticationFilter.CURRENT_USER_ATTR, email);
                    
                    log.info("Authentication set in SecurityContext");
                }
            } else {
                log.info("Token is invalid");
            }
        } catch (Exception e) {
            log.error("Error processing JWT token: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/ws/");
    }

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String AUTH_HEADER = "Authorization";
}
