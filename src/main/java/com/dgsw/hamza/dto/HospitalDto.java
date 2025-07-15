package com.dgsw.hamza.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public class HospitalDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HospitalInfo {
        private Long id;
        private String name;
        private String address;
        private String phone;
        private String website;
        private String description;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private String department;
        private BigDecimal rating;
        private String operatingHours;
        private LocalTime openTime;
        private LocalTime closeTime;
        private LocalTime lunchStart;
        private LocalTime lunchEnd;
        private Boolean isEmergency;
        private Boolean isActive;
        private Boolean parkingAvailable;
        private Boolean wheelchairAccessible;
        private String specializedTreatments;
        private Double distance; // 사용자로부터의 거리 (km)
        private Boolean isOpen; // 현재 운영 중인지
        private String googlePlaceId; // 구글 플레이스 ID
        private Integer userRatingsTotal; // 총 리뷰 수
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HospitalSearchRequest {
        private Double latitude;
        private Double longitude;
        private Integer radius; // 검색 반경 (미터)
        private String keyword; // 검색 키워드
        private Integer maxResults; // 최대 결과 수
        private String sortBy; // 정렬 기준: distance, rating, name
        private Boolean emergencyOnly; // 응급실만 검색
        private Boolean openNow; // 현재 영업 중인 곳만
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HospitalSearchResponse {
        private List<HospitalInfo> hospitals;
        private Integer totalCount;
        private String searchKeyword;
        private LocationDto searchLocation;
        private Integer searchRadius;
        private String message;
        private Boolean hasMore; // 더 많은 결과가 있는지
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationDto {
        private Double latitude;
        private Double longitude;
        private String address;
        private String city;
        private String district;
        private String country;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HospitalDetailRequest {
        private Long hospitalId;
        private String googlePlaceId;
        private Double userLatitude;
        private Double userLongitude;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HospitalDetailResponse {
        private HospitalInfo hospital;
        private List<String> services; // 제공 서비스
        private List<String> specialties; // 전문 분야
        private OpeningHours openingHours;
        private ContactInfo contactInfo;
        private LocationInfo locationInfo;
        private List<ReviewSummary> recentReviews;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OpeningHours {
        private Boolean openNow;
        private List<String> weekdayText;
        private List<DayHours> periods;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DayHours {
        private String day;
        private LocalTime openTime;
        private LocalTime closeTime;
        private Boolean closed;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ContactInfo {
        private String phone;
        private String website;
        private String email;
        private String googleUrl;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LocationInfo {
        private String fullAddress;
        private String streetAddress;
        private String city;
        private String district;
        private String postalCode;
        private String country;
        private Double latitude;
        private Double longitude;
        private String directions; // 길찾기 URL
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReviewSummary {
        private String authorName;
        private Integer rating;
        private String text;
        private String relativeTime;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GooglePlaceDto {
        private String placeId;
        private String name;
        private String vicinity;
        private String formattedAddress;
        private Double latitude;
        private Double longitude;
        private BigDecimal rating;
        private Integer userRatingsTotal;
        private String formattedPhoneNumber;
        private String website;
        private String url;
        private Boolean openNow;
        private List<String> types;
        private Integer priceLevel;
        private String businessStatus;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeocodeRequest {
        private String address;
        private String language;
        private String region;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeocodeResponse {
        private Double latitude;
        private Double longitude;
        private String formattedAddress;
        private String status;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NearbySearchRequest {
        private Double latitude;
        private Double longitude;
        private Integer radius;
        private String type;
        private String keyword;
        private String language;
        private Integer maxResults;
        private String pageToken; // 다음 페이지 토큰
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlaceDetailsRequest {
        private String placeId;
        private String fields;
        private String language;
    }
}