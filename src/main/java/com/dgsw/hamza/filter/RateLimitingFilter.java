package com.dgsw.hamza.filter;

import com.dgsw.hamza.config.RateLimitingConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig config;
    private final ObjectMapper objectMapper;
    
    // IP별 요청 카운터 저장
    private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();
    
    public RateLimitingFilter(RateLimitingConfig config, ObjectMapper objectMapper) {
        this.config = config;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        if (!config.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientIp = getClientIpAddress(request);
        String endpoint = request.getRequestURI();
        
        // 특정 엔드포인트별 제한 확인
        if (isRateLimited(clientIp, endpoint)) {
            sendRateLimitResponse(response, endpoint);
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private boolean isRateLimited(String clientIp, String endpoint) {
        RequestCounter counter = requestCounters.computeIfAbsent(clientIp, k -> new RequestCounter());
        
        // 채팅 API 특별 제한
        if (endpoint.startsWith("/api/chat/")) {
            return !counter.allowChatRequest(config.getChatRequestsPerMinute());
        }
        
        // 진단 API 특별 제한
        if (endpoint.startsWith("/api/diagnosis/")) {
            return !counter.allowDiagnosisRequest(config.getDiagnosisRequestsPerHour());
        }
        
        // 일반 API 제한
        return !counter.allowGeneralRequest(config.getRequestsPerMinute(), config.getRequestsPerHour());
    }
    
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    private void sendRateLimitResponse(HttpServletResponse response, String endpoint) throws IOException {
        response.setStatus(429); // TOO_MANY_REQUESTS
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorResponse = Map.of(
            "error", "Rate limit exceeded",
            "message", "Too many requests for endpoint: " + endpoint,
            "timestamp", Instant.now().toString()
        );
        
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
    
    private static class RequestCounter {
        private final AtomicInteger generalMinuteCount = new AtomicInteger(0);
        private final AtomicInteger generalHourCount = new AtomicInteger(0);
        private final AtomicInteger chatMinuteCount = new AtomicInteger(0);
        private final AtomicInteger diagnosisHourCount = new AtomicInteger(0);
        
        private volatile Instant lastMinuteReset = Instant.now();
        private volatile Instant lastHourReset = Instant.now();
        
        public synchronized boolean allowGeneralRequest(int minuteLimit, int hourLimit) {
            resetCountersIfNeeded();
            
            return generalMinuteCount.get() < minuteLimit && 
                   generalHourCount.get() < hourLimit &&
                   generalMinuteCount.incrementAndGet() <= minuteLimit &&
                   generalHourCount.incrementAndGet() <= hourLimit;
        }
        
        public synchronized boolean allowChatRequest(int minuteLimit) {
            resetCountersIfNeeded();
            
            return chatMinuteCount.get() < minuteLimit && 
                   chatMinuteCount.incrementAndGet() <= minuteLimit;
        }
        
        public synchronized boolean allowDiagnosisRequest(int hourLimit) {
            resetCountersIfNeeded();
            
            return diagnosisHourCount.get() < hourLimit && 
                   diagnosisHourCount.incrementAndGet() <= hourLimit;
        }
        
        private void resetCountersIfNeeded() {
            Instant now = Instant.now();
            
            if (ChronoUnit.MINUTES.between(lastMinuteReset, now) >= 1) {
                generalMinuteCount.set(0);
                chatMinuteCount.set(0);
                lastMinuteReset = now;
            }
            
            if (ChronoUnit.HOURS.between(lastHourReset, now) >= 1) {
                generalHourCount.set(0);
                diagnosisHourCount.set(0);
                lastHourReset = now;
            }
        }
    }
}