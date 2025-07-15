package com.dgsw.hamza.entity;

import com.dgsw.hamza.enums.AuthProvider;
import com.dgsw.hamza.enums.UserRole;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(name = "profile_image")
    private String profileImage;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "birth_date")
    private LocalDateTime birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserRole role = UserRole.USER;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "email_verified")
    @Builder.Default
    private Boolean emailVerified = false;

    // Relationships
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Diagnosis> diagnoses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Recommendation> recommendations = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<UserProgress> userProgress = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<ChatSession> chatSessions = new ArrayList<>();

    // Convenience methods
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isTherapist() {
        return role == UserRole.THERAPIST;
    }

    public boolean isEmailVerified() {
        return Boolean.TRUE.equals(emailVerified);
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(isActive);
    }

    public void updateLastLogin() {
        this.lastLogin = LocalDateTime.now();
    }
}