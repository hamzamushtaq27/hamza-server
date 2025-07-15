package com.dgsw.hamza.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "external-api.google")
@Getter
@Setter
public class GoogleMapsConfig {
    
    private String apiKey;
    private String placesUrl;
    private String geocodingUrl;
    
    // Google Maps API 관련 상수들
    public static class Constants {
        public static final String HOSPITAL_TYPE = "hospital";
        public static final String PSYCHIATRIST_KEYWORD = "정신건강의학과";
        public static final int DEFAULT_RADIUS = 10000; // 10km
        public static final int MAX_RESULTS = 20;
        public static final String LANGUAGE = "ko";
        public static final String REGION = "KR";
    }
    
    // Places API 기본 필드들
    public static class PlaceFields {
        public static final String BASIC_FIELDS = "place_id,name,geometry,formatted_address,rating,opening_hours,formatted_phone_number";
        public static final String CONTACT_FIELDS = "formatted_phone_number,website,url";
        public static final String ATMOSPHERE_FIELDS = "rating,user_ratings_total,price_level";
    }
    
    // 검색 타입
    public enum SearchType {
        NEARBY_SEARCH,
        TEXT_SEARCH,
        FIND_PLACE
    }
    
    public String getPlacesApiUrl() {
        return placesUrl;
    }
    
    public String getGeocodingApiUrl() {
        return geocodingUrl;
    }
    
    public String getApiKeyParam() {
        return "key=" + apiKey;
    }
    
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty() && 
               !apiKey.contains("your-google-maps-api-key");
    }
}