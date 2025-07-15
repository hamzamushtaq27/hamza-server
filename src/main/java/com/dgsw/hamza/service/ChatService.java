package com.dgsw.hamza.service;

import com.dgsw.hamza.dto.ChatDto;
import com.dgsw.hamza.entity.ChatMessage;
import com.dgsw.hamza.entity.ChatSession;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.ChatMessageType;
import com.dgsw.hamza.enums.CrisisLevel;
import com.dgsw.hamza.repository.ChatMessageRepository;
import com.dgsw.hamza.repository.ChatSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    /**
     * 간단한 채팅 메시지 처리
     */
    public ChatDto.ChatMessageResponse processMessage(User user, ChatDto.ChatMessageRequest request) {
        log.info("사용자 {} 메시지 처리: {}", user.getId(), request.getMessage());

        // 1. 세션 조회 또는 생성
        ChatSession session = getOrCreateSession(user, request.getSessionId());

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .messageContent(request.getMessage())
                .isFromUser(true)
                .build();
        messageRepository.save(userMessage);

        // 3. 간단한 봇 응답 생성
        String botResponse = generateSimpleResponse(request.getMessage());

        // 4. 봇 메시지 저장
        ChatMessage botMessage = ChatMessage.builder()
                .chatSession(session)
                .messageContent(botResponse)
                .isFromUser(false)
                .build();
        messageRepository.save(botMessage);

        // 5. 응답 생성
        return ChatDto.ChatMessageResponse.builder()
                .messageId(botMessage.getId())
                .sessionId(session.getId().toString())
                .response(botResponse)
                .messageType(ChatMessageType.BOT)
                .crisisLevel(CrisisLevel.NONE)
                .crisisDetected(false)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 채팅 히스토리 조회 (간단 버전)
     */
    @Transactional(readOnly = true)
    public ChatDto.ChatHistoryResponse getChatHistory(User user, ChatDto.ChatHistoryRequest request) {
        log.info("사용자 {} 채팅 히스토리 조회", user.getId());

        // 간단한 히스토리 응답
        return ChatDto.ChatHistoryResponse.builder()
                .messages(List.of())
                .totalCount(0)
                .build();
    }

    /**
     * 활성 세션 조회
     */
    @Transactional(readOnly = true)
    public List<ChatDto.ChatSessionInfo> getActiveSessions(User user) {
        log.info("사용자 {} 활성 세션 조회", user.getId());
        return List.of();
    }

    /**
     * 세션 종료
     */
    public void endSession(User user, String sessionId) {
        log.info("사용자 {} 세션 {} 종료", user.getId(), sessionId);
        // 간단한 구현
    }

    /**
     * 위기 상황 감지 알림
     */
    public void handleCrisisAlert(User user, String sessionId, CrisisLevel crisisLevel) {
        log.warn("사용자 {} 위기 상황 감지 - 수준: {}", user.getId(), crisisLevel);
        // 간단한 구현
    }

    /**
     * 챗봇 상태 조회
     */
    @Transactional(readOnly = true)
    public ChatDto.ChatbotStatus getChatbotStatus() {
        log.info("챗봇 상태 조회");

        return ChatDto.ChatbotStatus.builder()
                .isActive(true)
                .activeSessionCount(0)
                .todayMessageCount(0)
                .crisisDetectionCount(0)
                .averageResponseTime(150L)
                .lastUpdateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 감정 분석 보고서 생성
     */
    @Transactional(readOnly = true)
    public Object generateEmotionReport(User user, int days) {
        log.info("사용자 {} 감정 분석 보고서 생성 - {}일", user.getId(), days);

        return new Object() {
            public final String message = "감정 분석 보고서 (간단 버전)";
            public final LocalDateTime generatedAt = LocalDateTime.now();
        };
    }

    // Private helper methods

    private ChatSession getOrCreateSession(User user, String sessionId) {
        if (sessionId != null) {
            try {
                Long id = Long.parseLong(sessionId);
                return sessionRepository.findById(id).orElse(null);
            } catch (NumberFormatException e) {
                // 잘못된 세션 ID 형식
            }
        }

        // 새 세션 생성
        ChatSession session = ChatSession.builder()
                .user(user)
                .build();

        return sessionRepository.save(session);
    }

    private String generateSimpleResponse(String userMessage) {
        String message = userMessage.toLowerCase();
        
        if (message.contains("안녕") || message.contains("hello")) {
            return "안녕하세요! 오늘 기분은 어떠신가요?";
        } else if (message.contains("우울") || message.contains("슬퍼")) {
            return "우울한 기분이시군요. 이런 감정을 느끼는 것은 자연스러운 일입니다.";
        } else if (message.contains("불안") || message.contains("걱정")) {
            return "불안한 마음이 드시는군요. 깊게 숨을 들이마시고 천천히 내쉬어보세요.";
        } else if (message.contains("고마워") || message.contains("감사")) {
            return "천만에요! 언제든 도움이 필요하시면 말씀해 주세요.";
        } else {
            return "말씀해 주신 내용을 잘 들었습니다. 더 자세히 이야기해 주실 수 있나요?";
        }
    }
}