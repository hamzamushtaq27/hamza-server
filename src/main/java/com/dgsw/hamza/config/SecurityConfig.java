package com.dgsw.hamza.config;

import com.dgsw.hamza.security.CustomUserDetailsService;
import com.dgsw.hamza.security.JwtAuthenticationEntryPoint;
import com.dgsw.hamza.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOriginPatterns(java.util.List.of("*"));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);
                return corsConfig;
            }))
            
            // CSRF 비활성화 (JWT 사용으로 불필요)
            .csrf(AbstractHttpConfigurer::disable)
            
            // 기본 HTTP 인증 비활성화
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // 폼 로그인 비활성화
            .formLogin(AbstractHttpConfigurer::disable)
            
            // 세션 정책 설정 (Stateless)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 예외 처리
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            )
            
            // 보안 헤더 설정
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.deny())
                .contentTypeOptions(contentTypeOptions -> {})
                .httpStrictTransportSecurity(hstsConfig -> {})
                .referrerPolicy(referrerPolicy -> referrerPolicy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            )
            
            // URL 별 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 인증 없이 접근 가능한 경로
                .requestMatchers(
                    "/api/auth/signin", "/api/auth/signup", "/api/auth/refresh",
                    "/api/auth/signIn", "/api/auth/signUp"
                ).permitAll()
                .requestMatchers("/api/auth/check-email", "/api/auth/check-nickname").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // Swagger UI 접근 허용
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-resources/**", "/webjars/**").permitAll()
                
                // H2 Console 접근 허용 (개발 환경)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Actuator 접근 허용
                .requestMatchers("/actuator/**").permitAll()
                
                // 정적 리소스 접근 허용
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**").permitAll()
                
                // 에러 페이지 접근 허용
                .requestMatchers("/error").permitAll()
                
                // 관리자 전용 경로
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 상담사 전용 경로
                .requestMatchers("/api/therapist/**").hasAnyRole("THERAPIST", "ADMIN")
                
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            );
        
        // JWT 필터 추가
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}