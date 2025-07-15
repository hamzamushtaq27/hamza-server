package com.dgsw.hamza.security;

import com.dgsw.hamza.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String nickname;
    private final String role;
    private final boolean isActive;
    private final boolean emailVerified;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String email, String password, String nickname, 
                        String role, boolean isActive, boolean emailVerified,
                        Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role;
        this.isActive = isActive;
        this.emailVerified = emailVerified;
        this.authorities = authorities;
    }

    public static UserPrincipal create(User user) {
        Collection<GrantedAuthority> authorities = Collections.singletonList(
            new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
        );

        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getPassword(),
            user.getNickname(),
            user.getRole().name(),
            user.isActive(),
            user.isEmailVerified(),
            authorities
        );
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return isActive;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isActive && emailVerified;
    }

    // 추가 편의 메서드
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }

    public boolean isTherapist() {
        return "THERAPIST".equals(role);
    }

    public boolean isUser() {
        return "USER".equals(role);
    }
}