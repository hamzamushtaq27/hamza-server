package com.dgsw.hamza.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocationCalculator {

    private static final double EARTH_RADIUS_KM = 6371.0;

    private LocationCalculator() {
        // Utility class - prevent instantiation
    }

    /**
     * 두 지점 간의 거리를 계산합니다 (Haversine formula)
     * @param lat1 첫 번째 지점의 위도
     * @param lon1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lon2 두 번째 지점의 경도
     * @return 두 지점 간의 거리 (km)
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0;
        }

        // Convert degrees to radians
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine formula
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * 미터를 킬로미터로 변환
     */
    public static double metersToKilometers(double meters) {
        return meters / 1000.0;
    }

    /**
     * 킬로미터를 미터로 변환
     */
    public static double kilometersToMeters(double kilometers) {
        return kilometers * 1000.0;
    }

    /**
     * 거리를 사용자 친화적인 문자열로 변환
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            return String.format("%.0fm", distanceKm * 1000);
        } else if (distanceKm < 10.0) {
            return String.format("%.1fkm", distanceKm);
        } else {
            return String.format("%.0fkm", distanceKm);
        }
    }

    /**
     * 위도/경도가 유효한지 검증
     */
    public static boolean isValidLatitude(double latitude) {
        return latitude >= -90.0 && latitude <= 90.0;
    }

    /**
     * 경도가 유효한지 검증
     */
    public static boolean isValidLongitude(double longitude) {
        return longitude >= -180.0 && longitude <= 180.0;
    }

    /**
     * 위도/경도 좌표가 유효한지 검증
     */
    public static boolean isValidCoordinate(double latitude, double longitude) {
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }

    /**
     * 한국 내 좌표인지 검증 (대략적인 범위)
     */
    public static boolean isKoreanCoordinate(double latitude, double longitude) {
        // 한국의 대략적인 경계
        return latitude >= 33.0 && latitude <= 43.0 && 
               longitude >= 124.0 && longitude <= 132.0;
    }

    /**
     * 두 좌표 간의 방향 계산 (북쪽 기준 시계방향 각도)
     */
    public static double calculateBearing(double lat1, double lon1, double lat2, double lon2) {
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLonRad = Math.toRadians(lon2 - lon1);

        double y = Math.sin(deltaLonRad) * Math.cos(lat2Rad);
        double x = Math.cos(lat1Rad) * Math.sin(lat2Rad) - 
                   Math.sin(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad);

        double bearingRad = Math.atan2(y, x);
        double bearingDeg = Math.toDegrees(bearingRad);

        return (bearingDeg + 360) % 360;
    }

    /**
     * 방향을 문자열로 변환
     */
    public static String formatDirection(double bearing) {
        String[] directions = {"북", "북동", "동", "남동", "남", "남서", "서", "북서"};
        int index = (int) Math.round(bearing / 45.0) % 8;
        return directions[index];
    }

    /**
     * 중심점에서 반경 내의 경계 좌표 계산
     */
    public static BoundingBox calculateBoundingBox(double centerLat, double centerLon, double radiusKm) {
        // 위도 1도 = 약 111km
        // 경도 1도 = 약 111km * cos(위도)
        double latDelta = radiusKm / 111.0;
        double lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(centerLat)));

        return new BoundingBox(
            centerLat - latDelta,  // 남쪽 경계
            centerLat + latDelta,  // 북쪽 경계
            centerLon - lonDelta,  // 서쪽 경계
            centerLon + lonDelta   // 동쪽 경계
        );
    }

    /**
     * 경계 박스 클래스
     */
    public static class BoundingBox {
        public final double south;
        public final double north;
        public final double west;
        public final double east;

        public BoundingBox(double south, double north, double west, double east) {
            this.south = south;
            this.north = north;
            this.west = west;
            this.east = east;
        }

        public boolean contains(double lat, double lon) {
            return lat >= south && lat <= north && lon >= west && lon <= east;
        }
    }

    /**
     * 좌표가 특정 반경 내에 있는지 확인
     */
    public static boolean isWithinRadius(double centerLat, double centerLon, 
                                       double targetLat, double targetLon, 
                                       double radiusKm) {
        double distance = calculateDistance(centerLat, centerLon, targetLat, targetLon);
        return distance <= radiusKm;
    }

    /**
     * 가장 가까운 지점 찾기
     */
    public static int findNearestPoint(double targetLat, double targetLon, 
                                     double[] latitudes, double[] longitudes) {
        if (latitudes.length != longitudes.length) {
            throw new IllegalArgumentException("위도와 경도 배열의 길이가 다릅니다.");
        }

        int nearestIndex = 0;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < latitudes.length; i++) {
            double distance = calculateDistance(targetLat, targetLon, latitudes[i], longitudes[i]);
            if (distance < minDistance) {
                minDistance = distance;
                nearestIndex = i;
            }
        }

        return nearestIndex;
    }

    /**
     * 구글 맵스 URL 생성
     */
    public static String generateGoogleMapsUrl(double latitude, double longitude) {
        return String.format("https://www.google.com/maps?q=%.6f,%.6f", latitude, longitude);
    }

    /**
     * 길찾기 URL 생성
     */
    public static String generateDirectionsUrl(double fromLat, double fromLon, 
                                             double toLat, double toLon) {
        return String.format("https://www.google.com/maps/dir/%.6f,%.6f/%.6f,%.6f", 
                           fromLat, fromLon, toLat, toLon);
    }

    /**
     * 좌표 로깅용 (디버깅)
     */
    public static void logCoordinate(String label, double latitude, double longitude) {
        log.debug("{}: ({}, {})", label, latitude, longitude);
    }
}