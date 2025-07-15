package com.dgsw.hamza.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test") // 테스트 환경에서는 제외
public class HealthCheckConfig {
    
    // Spring Boot Actuator의 기본 health check 사용
    // 필요한 경우 커스텀 health indicator 추가 가능
    
}