package com.dgsw.hamza.security;

import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", email);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email);
                });

        if (!user.isActive()) {
            log.error("User account is deactivated: {}", email);
            throw new UsernameNotFoundException("비활성화된 계정입니다: " + email);
        }

        log.debug("User loaded successfully: {}", email);
        return UserPrincipal.create(user);
    }

    @Transactional(readOnly = true)
    public UserDetails loadUserById(Long id) {
        log.debug("Loading user by id: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with id: {}", id);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + id);
                });

        if (!user.isActive()) {
            log.error("User account is deactivated: {}", id);
            throw new UsernameNotFoundException("비활성화된 계정입니다: " + id);
        }

        log.debug("User loaded successfully: {}", id);
        return UserPrincipal.create(user);
    }
}