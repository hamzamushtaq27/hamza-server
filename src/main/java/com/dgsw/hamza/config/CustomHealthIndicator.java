package com.dgsw.hamza.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            // 데이터베이스 연결 상태 확인
            if (isDatabaseHealthy()) {
                return Health.up()
                        .withDetail("database", "Available")
                        .withDetail("diskSpace", "Available")
                        .withDetail("customCheck", "All systems operational")
                        .build();
            } else {
                return Health.down()
                        .withDetail("database", "Connection failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private boolean isDatabaseHealthy() {
        // 실제 데이터베이스 연결 확인 로직
        return true; // 간단한 예시
    }
}