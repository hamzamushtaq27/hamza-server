package com.dgsw.hamza.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitingConfig {
    
    private int requestsPerMinute = 60;
    private int requestsPerHour = 1000;
    private int chatRequestsPerMinute = 10;
    private int diagnosisRequestsPerHour = 5;
    private boolean enabled = true;
    
    // Getters and setters
    public int getRequestsPerMinute() {
        return requestsPerMinute;
    }
    
    public void setRequestsPerMinute(int requestsPerMinute) {
        this.requestsPerMinute = requestsPerMinute;
    }
    
    public int getRequestsPerHour() {
        return requestsPerHour;
    }
    
    public void setRequestsPerHour(int requestsPerHour) {
        this.requestsPerHour = requestsPerHour;
    }
    
    public int getChatRequestsPerMinute() {
        return chatRequestsPerMinute;
    }
    
    public void setChatRequestsPerMinute(int chatRequestsPerMinute) {
        this.chatRequestsPerMinute = chatRequestsPerMinute;
    }
    
    public int getDiagnosisRequestsPerHour() {
        return diagnosisRequestsPerHour;
    }
    
    public void setDiagnosisRequestsPerHour(int diagnosisRequestsPerHour) {
        this.diagnosisRequestsPerHour = diagnosisRequestsPerHour;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}