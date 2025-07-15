package com.dgsw.hamza.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class NgrokHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // ngrok 관련 헤더 처리
        String origin = request.getHeader("Origin");
        if (origin != null && (origin.contains("ngrok-free.app") || origin.contains("ngrok.io"))) {
            // ngrok 브라우저 경고 스킵 헤더 추가
            response.setHeader("ngrok-skip-browser-warning", "true");
        }
        
        // OPTIONS 요청 (preflight) 처리
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, PATCH");
            response.setHeader("Access-Control-Allow-Headers", 
                "Origin, Content-Type, Accept, Authorization, X-Requested-With, X-Auth-Token, X-Xsrf-Token, Cache-Control, Id-Token, ngrok-skip-browser-warning");
            response.setHeader("Access-Control-Allow-Credentials", "true");
            response.setHeader("Access-Control-Max-Age", "86400");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}