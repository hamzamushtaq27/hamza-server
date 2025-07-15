package com.dgsw.hamza.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS 설정은 SecurityConfig에서 통합 관리
    // 중복 설정 방지를 위해 여기서는 제거
}