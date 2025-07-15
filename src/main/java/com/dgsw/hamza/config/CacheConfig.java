package com.dgsw.hamza.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    
    public static final String HOSPITALS_CACHE = "hospitals";
    public static final String TREATMENTS_CACHE = "treatments";
    public static final String DIAGNOSIS_QUESTIONS_CACHE = "diagnosis_questions";
    public static final String USER_PROFILE_CACHE = "user_profile";
    public static final String RECOMMENDATIONS_CACHE = "recommendations";
    
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        cacheManager.setCacheNames(java.util.Arrays.asList(
            HOSPITALS_CACHE,
            TREATMENTS_CACHE,
            DIAGNOSIS_QUESTIONS_CACHE,
            USER_PROFILE_CACHE,
            RECOMMENDATIONS_CACHE
        ));
        return cacheManager;
    }
}