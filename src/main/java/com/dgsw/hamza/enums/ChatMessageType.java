package com.dgsw.hamza.enums;

public enum ChatMessageType {
    USER("사용자 메시지"),
    BOT("봇 메시지"),
    SYSTEM("시스템 메시지"),
    CRISIS_ALERT("위기 상황 알림");

    private final String description;

    ChatMessageType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}