package com.dgsw.hamza.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "hospitals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(name = "phone")
    private String phone;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "department")
    private String department;

    @Column(name = "website")
    private String website;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "operating_hours")
    private String operatingHours;

    @Column(name = "open_time")
    private LocalTime openTime;

    @Column(name = "close_time")
    private LocalTime closeTime;

    @Column(name = "lunch_start")
    private LocalTime lunchStart;

    @Column(name = "lunch_end")
    private LocalTime lunchEnd;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "is_emergency")
    @Builder.Default
    private Boolean isEmergency = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "specialized_treatments")
    private String specializedTreatments;

    @Column(name = "parking_available")
    @Builder.Default
    private Boolean parkingAvailable = false;

    @Column(name = "wheelchair_accessible")
    @Builder.Default
    private Boolean wheelchairAccessible = false;

    // Convenience methods
    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public boolean isEmergency() {
        return Boolean.TRUE.equals(isEmergency);
    }

    public boolean hasParkingAvailable() {
        return Boolean.TRUE.equals(parkingAvailable);
    }

    public boolean isWheelchairAccessible() {
        return Boolean.TRUE.equals(wheelchairAccessible);
    }

    public boolean isCurrentlyOpen() {
        if (openTime == null || closeTime == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        return now.isAfter(openTime) && now.isBefore(closeTime);
    }

    public boolean isInLunchTime() {
        if (lunchStart == null || lunchEnd == null) {
            return false;
        }
        LocalTime now = LocalTime.now();
        return now.isAfter(lunchStart) && now.isBefore(lunchEnd);
    }

    public double getDistanceFrom(double userLat, double userLng) {
        if (latitude == null || longitude == null) {
            return Double.MAX_VALUE;
        }
        
        double lat1 = Math.toRadians(userLat);
        double lat2 = Math.toRadians(latitude.doubleValue());
        double lng1 = Math.toRadians(userLng);
        double lng2 = Math.toRadians(longitude.doubleValue());
        
        double deltaLat = lat2 - lat1;
        double deltaLng = lng2 - lng1;
        
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return 6371 * c; // Earth's radius in kilometers
    }
}