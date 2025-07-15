package com.dgsw.hamza.enums;

public enum AuthProvider {
    LOCAL("자체 로그인"),
    GOOGLE("구글 로그인"),
    KAKAO("카카오 로그인"),
    NAVER("네이버 로그인");

    private final String description;

    AuthProvider(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}