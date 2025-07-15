package com.dgsw.hamza.service;

import com.dgsw.hamza.dto.ChatDto;
import com.dgsw.hamza.entity.ChatMessage;
import com.dgsw.hamza.entity.ChatSession;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.ChatMessageType;
import com.dgsw.hamza.enums.CrisisLevel;
import com.dgsw.hamza.repository.ChatMessageRepository;
import com.dgsw.hamza.repository.ChatSessionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;

    @Value("${openai.api-key}")
    private String openaiApiKey;

    private WebClient webClient;

    @PostConstruct
    private void initWebClient() {
        this.webClient = WebClient.builder()
            .baseUrl("https://api.openai.com/v1")
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
            .build();
    }

    private String generateAIResponse(String userMessage) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo-0125");
        JSONArray messages = new JSONArray();
        messages.put(new JSONObject().put("role", "user").put("content", userMessage));
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 64);
        requestBody.put("temperature", 0.5);

        Mono<String> responseMono = webClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(requestBody.toString())
            .retrieve()
            .bodyToMono(String.class);

        String response = responseMono.block(); // 동기적으로 결과 받기(그래도 RestTemplate보다 빠름)
        JSONObject responseBody = new JSONObject(response);
        String aiReply = responseBody
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content");

        return aiReply.trim();
    }

    /**
     * 간단한 채팅 메시지 처리
     */
    public ChatDto.ChatMessageResponse processMessage(User user, ChatDto.ChatMessageRequest request) {
        log.info("사용자 {} 메시지 처리: {}", user.getId(), request.getMessage());

        // 1. 세션 조회 또는 생성
        ChatSession session = getOrCreateSession(user, request.getSessionId());
        if (session == null) {
            throw new IllegalStateException("채팅 세션을 생성할 수 없습니다.");
        }

        // 2. 사용자 메시지 저장
        ChatMessage userMessage = ChatMessage.builder()
                .chatSession(session)
                .messageContent(request.getMessage())
                .isFromUser(true)
                .build();
        messageRepository.save(userMessage);

        // 3. AI 챗봇 응답 생성
        String botResponse = generateAIResponse(request.getMessage());

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
                .sessionId(session.getId())
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

        ChatSession session = null;
        if (request.getSessionId() != null) {
            try {
                Long sessionId = request.getSessionId();
                session = sessionRepository.findById(sessionId).orElse(null);
            } catch (NumberFormatException e) {
                // 잘못된 세션 ID
            }
        }
        if (session == null) {
            return ChatDto.ChatHistoryResponse.builder()
                    .messages(List.of())
                    .totalCount(0)
                    .build();
        }
        // 메시지 조회 (필터/페이징/기간 등은 간단화)
        List<ChatMessage> messages = messageRepository.findByChatSessionOrderByCreatedAt(session);
        List<ChatDto.ChatMessageHistory> messageDtos = messages.stream().map(m ->
                ChatDto.ChatMessageHistory.builder()
                        .messageId(m.getId())
                        .content(m.getMessageContent())
                        .messageType(m.isFromUser() ? ChatMessageType.USER : ChatMessageType.BOT)
                        .crisisLevel(m.getCrisisLevel() != null ? m.getCrisisLevel() : CrisisLevel.NONE)
                        .timestamp(m.getCreatedAt())
                        .emotionAnalysis(null) // 감정 분석 결과 추가 가능
                        .build()
        ).toList();
        int totalCount = messages.size();
        // 세션 정보
        ChatDto.ChatSessionInfo sessionInfo = ChatDto.ChatSessionInfo.builder()
                .sessionId(session.getId())
                .userId(user.getId())
                .startTime(session.getCreatedAt())
                .lastActivity(session.getEndedAt() != null ? session.getEndedAt() : session.getCreatedAt())
                .messageCount(totalCount)
                .crisisDetectionCount((int) messages.stream().filter(m -> m.getCrisisLevel() != null && m.getCrisisLevel() != CrisisLevel.NONE).count())
                .isActive(session.getIsActive())
                .sessionSummary("")
                .build();
        // 위기 통계 (간단화)
        ChatDto.CrisisStatistics crisisStats = ChatDto.CrisisStatistics.builder()
                .totalMessages(totalCount)
                .crisisMessages((int) messages.stream().filter(m -> m.getCrisisLevel() != null && m.getCrisisLevel() != CrisisLevel.NONE).count())
                .highestCrisisLevel(messages.stream().map(ChatMessage::getCrisisLevel).filter(l -> l != null).max(Enum::compareTo).orElse(CrisisLevel.NONE))
                .crisisRate(totalCount == 0 ? 0.0 : 100.0 * messages.stream().filter(m -> m.getCrisisLevel() != null && m.getCrisisLevel() != CrisisLevel.NONE).count() / (double) totalCount)
                .crisisDistribution(List.of()) // 분포 추가 가능
                .build();
        return ChatDto.ChatHistoryResponse.builder()
                .messages(messageDtos)
                .totalCount(totalCount)
                .sessionInfo(sessionInfo)
                .crisisStats(crisisStats)
                .build();
    }

    /**
     * 활성 세션 조회
     */
    @Transactional(readOnly = true)
    public List<ChatDto.ChatSessionInfo> getActiveSessions(User user) {
        log.info("사용자 {} 활성 세션 조회", user.getId());
        List<ChatSession> sessions = sessionRepository.findActiveSessionsByUser(user);
        List<ChatDto.ChatSessionInfo> list = sessions.stream().map(s -> {
            ChatDto.ChatSessionInfo build = ChatDto.ChatSessionInfo.builder()
                    .sessionId(s.getId())
                    .userId(user.getId())
                    .startTime(s.getCreatedAt())
                    .lastActivity(s.getEndedAt() != null ? s.getEndedAt() : s.getCreatedAt())
                    .messageCount(Math.toIntExact(messageRepository.countByChatSession(s)))
                    .crisisDetectionCount((int) messageRepository.findByChatSessionOrderByCreatedAt(s).stream().filter(m -> m.getCrisisLevel() != null && m.getCrisisLevel() != CrisisLevel.NONE).count())
                    .isActive(s.getIsActive())
                    .sessionSummary("")
                    .build();
            return build;
                }
        ).toList();
        return list;
    }

    /**
     * 세션 종료
     */
    public void endSession(User user, Long sessionId) {
        log.info("사용자 {} 세션 {} 종료", user.getId(), sessionId);
        if (sessionId == null) return;
        sessionRepository.findById(sessionId).ifPresent(session -> {
            if (session.getUser().getId().equals(user.getId()) && session.getIsActive()) {
                session.setIsActive(false);
                session.setEndedAt(LocalDateTime.now());
                sessionRepository.save(session);
            }
        });
    }

    /**
     * 위기 상황 감지 알림
     */
    public void handleCrisisAlert(User user, Long sessionId, CrisisLevel crisisLevel) {
        log.warn("사용자 {} 위기 상황 감지 - 수준: {}", user.getId(), crisisLevel);
        if (sessionId == null) return;
        sessionRepository.findById(sessionId).ifPresent(session -> {
            session.setCrisisLevelEnum(crisisLevel);
            sessionRepository.save(session);
        });
    }

    /**
     * 챗봇 상태 조회
     */
    @Transactional(readOnly = true)
    public ChatDto.ChatbotStatus getChatbotStatus() {
        log.info("챗봇 상태 조회");
        int activeSessionCount = sessionRepository.countActiveSessions().intValue();
        int todayMessageCount = messageRepository.countTodayMessages().intValue();
        int crisisDetectionCount = messageRepository.findCrisisMessages().size();
        // 평균 응답 시간은 샘플로 150L 유지
        return ChatDto.ChatbotStatus.builder()
                .isActive(true)
                .activeSessionCount(activeSessionCount)
                .todayMessageCount(todayMessageCount)
                .crisisDetectionCount(crisisDetectionCount)
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
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        var trend = messageRepository.findEmotionScoreTrend(user, since);
        Double avg = messageRepository.findAverageEmotionScoreByUser(user);
        Object stats = messageRepository.findEmotionStatsByUser(user);
        return new Object() {
            public final String message = "감정 분석 보고서";
            public final LocalDateTime generatedAt = LocalDateTime.now();
            public final Object emotionTrend = trend;
            public final Double averageScore = avg;
            public final Object emotionStats = stats;
        };
    }

    // Private helper methods

    private ChatSession getOrCreateSession(User user, Long sessionId) {
        if (sessionId != null) {
            ChatSession found = sessionRepository.findById(sessionId).orElse(null);
            if (found != null) return found;
        }
        ChatSession session = ChatSession.builder()
                .user(user)
                .build();
        return sessionRepository.save(session);
    }
}