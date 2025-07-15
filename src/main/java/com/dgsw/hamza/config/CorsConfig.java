package com.dgsw.hamza.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 오리진 설정
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "https://hamza.com",
                "https://*.hamza.com",
                "http://192.168.0.228:*"
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "X-API-Key",
                "X-User-Agent"
        ));
        
        // 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Total-Count"
        ));
        
        // 인증 정보 포함 허용
        configuration.setAllowCredentials(true);
        
        // 프리플라이트 요청 캐시 시간 (1시간)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        
        return source;
    }
}