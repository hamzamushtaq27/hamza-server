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
import java.util.regex.Pattern;

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

    // 위기 상황 감지를 위한 키워드 패턴들
    private static final Pattern CRISIS_KEYWORDS = Pattern.compile(
            ".*?(죽고\\s*싶|자살|목숨|끝내고\\s*싶|살기\\s*힘들|죽어버릴|더\\s*이상\\s*못\\s*살|생을\\s*마감|세상을\\s*떠나고|숨쉬기\\s*힘들|희망이\\s*없).*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern SEVERE_DEPRESSION_KEYWORDS = Pattern.compile(
            ".*?(심각한\\s*우울|극도의\\s*절망|모든\\s*게\\s*의미없|아무것도\\s*할\\s*수\\s*없|완전히\\s*무너져|지옥같은|견딜\\s*수\\s*없).*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern ADDICTION_KEYWORDS = Pattern.compile(
            ".*?(중독|끊을\\s*수\\s*없|조절이\\s*안\\s*돼|계속\\s*하게\\s*돼|멈출\\s*수\\s*없|의존|금단|재발|갈망).*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    private static final Pattern PTSD_KEYWORDS = Pattern.compile(
            ".*?(트라우마|악몽|플래시백|과거가\\s*떠올라|그때\\s*생각이\\s*나|무서운\\s*기억|잠들기\\s*무서|계속\\s*생각나).*",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    @PostConstruct
    private void initWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + openaiApiKey)
                .build();
    }

    private String generateAIResponse(String userMessage, List<ChatMessage> recentMessages) {
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo-0125");

        JSONArray messages = new JSONArray();

        // 시스템 프롬프트 추가 - 정신건강 상담 전문가 역할
        JSONObject systemMessage = new JSONObject();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSystemPrompt());
        messages.put(systemMessage);

        // 최근 대화 내역 추가 (컨텍스트 제공)
        if (recentMessages != null && !recentMessages.isEmpty()) {
            for (ChatMessage msg : recentMessages.subList(Math.max(0, recentMessages.size() - 10), recentMessages.size())) {
                JSONObject historyMessage = new JSONObject();
                historyMessage.put("role", msg.isFromUser() ? "user" : "assistant");
                historyMessage.put("content", msg.getMessageContent());
                messages.put(historyMessage);
            }
        }

        // 현재 사용자 메시지 추가
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.put(userMsg);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500); // 충분한 토큰 수로 증가
        requestBody.put("temperature", 0.7); // 약간 더 창의적인 응답을 위해 조정
        requestBody.put("presence_penalty", 0.1); // 반복을 줄이기 위해
        requestBody.put("frequency_penalty", 0.1);

        try {
            Mono<String> responseMono = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody.toString())
                    .retrieve()
                    .bodyToMono(String.class);

            String response = responseMono.block();
            JSONObject responseBody = new JSONObject(response);
            String aiReply = responseBody
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

            return aiReply.trim();
        } catch (Exception e) {
            log.error("AI 응답 생성 중 오류 발생", e);
            return "죄송합니다. 현재 시스템에 일시적인 문제가 있습니다. 잠시 후 다시 시도해주세요. 급한 상황이시라면 전문 상담 기관에 연락해주세요.";
        }
    }

    private String buildSystemPrompt() {
        return """
            당신은 정신건강 전문 상담 AI입니다. 다음 지침을 따라 대화해주세요:

            **역할과 태도:**
            - 따뜻하고 공감적인 정신건강 상담사로서 대화
            - 판단하지 말고 이해하려고 노력
            - 사용자의 감정을 인정하고 validation 제공
            - 전문적이면서도 친근한 말투 사용

            **대화 원칙:**
            - 사용자의 말을 끝까지 듣고 공감하기
            - 구체적이고 실용적인 조언 제공
            - 희망적인 메시지와 함께 현실적인 해결책 제시
            - 사용자의 강점과 자원 찾아내기

            **전문 분야:**
            - 우울증, 불안장애 상담
            - PTSD 및 트라우마 케어
            - 중독 회복 지원
            - 스트레스 관리 및 감정 조절
            - 자살 예방 및 위기 개입

            **위기 상황 대응:**
            - 자살 위험 징후 감지 시 즉시 전문기관 연계 권유
            - 긴급 상황에서는 119, 정신건강 상담전화 등 안내
            - 위기 상황에서도 희망 메시지 잊지 말기

            **주의사항:**
            - 의학적 진단이나 처방은 하지 않음
            - 전문의 치료가 필요한 경우 병원 방문 권유
            - 사용자의 비밀 보장 약속
            - 종교적, 정치적 편향 피하기

            응답은 한국어로 하되, 자연스럽고 따뜻한 말투로 대화해주세요.
            """;
    }

    /**
     * 위기 수준 감지 메서드
     */
    private CrisisLevel detectCrisisLevel(String message) {
        if (CRISIS_KEYWORDS.matcher(message).matches()) {
            return CrisisLevel.HIGH;
        } else if (SEVERE_DEPRESSION_KEYWORDS.matcher(message).matches()) {
            return CrisisLevel.MEDIUM;
        } else if (ADDICTION_KEYWORDS.matcher(message).matches() ||
                PTSD_KEYWORDS.matcher(message).matches()) {
            return CrisisLevel.LOW;
        }
        return CrisisLevel.NONE;
    }

    /**
     * 위기 상황 대응 메시지 생성
     */
    private String generateCrisisResponse(CrisisLevel crisisLevel, String originalResponse) {
        String crisisMessage = switch (crisisLevel) {
            case HIGH -> """
                
                🚨 **긴급 상황 안내**
                지금 힘든 상황에 계신 것 같습니다. 혼자 견디지 마시고 전문가의 도움을 받으세요.
                
                • 자살예방 상담전화: 109 (24시간)
                • 정신건강 상담전화: 1577-0199
                • 응급상황: 119
                
                당신의 생명은 소중합니다. 지금 이 순간을 견뎌내시면 분명 나아질 수 있습니다.
                """;
            case MEDIUM -> """
                
                💙 **전문 상담 권유**
                많이 힘드신 것 같습니다. 전문가와 상담받아보시는 것을 권합니다.
                
                • 정신건강 상담전화: 1577-0199
                • 가까운 정신건강복지센터 방문
                • 병원 정신건강의학과 상담
                """;
            case LOW -> """
                
                🤗 **지지와 격려**
                어려운 시간을 보내고 계시는군요. 혼자가 아니라는 것을 기억해주세요.
                필요하시면 언제든 전문 상담을 받아보세요.
                """;
            default -> "";
        };

        return originalResponse + crisisMessage;
    }

    /**
     * 개선된 메시지 처리
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

        // 3. 위기 수준 감지
        CrisisLevel crisisLevel = detectCrisisLevel(request.getMessage());

        // 4. 최근 대화 내역 조회 (컨텍스트 제공)
        List<ChatMessage> recentMessages = messageRepository.findByChatSessionOrderByCreatedAt(session);

        // 5. AI 챗봇 응답 생성 (컨텍스트 포함)
        String botResponse = generateAIResponse(request.getMessage(), recentMessages);

        // 6. 위기 상황 대응 메시지 추가
        if (crisisLevel != CrisisLevel.NONE) {
            botResponse = generateCrisisResponse(crisisLevel, botResponse);
        }

        // 7. 봇 메시지 저장
        ChatMessage botMessage = ChatMessage.builder()
                .chatSession(session)
                .messageContent(botResponse)
                .isFromUser(false)
                .crisisLevel(crisisLevel)
                .build();
        messageRepository.save(botMessage);

        // 8. 위기 상황 알림 처리
        if (crisisLevel != CrisisLevel.NONE) {
            handleCrisisAlert(user, session.getId(), crisisLevel);
        }

        // 9. 응답 생성
        return ChatDto.ChatMessageResponse.builder()
                .messageId(botMessage.getId())
                .sessionId(session.getId())
                .response(botResponse)
                .messageType(ChatMessageType.BOT)
                .crisisLevel(crisisLevel)
                .crisisDetected(crisisLevel != CrisisLevel.NONE)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // 나머지 메서드들은 기존과 동일하게 유지...

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
        return sessions.stream().map(s ->
                ChatDto.ChatSessionInfo.builder()
                        .sessionId(s.getId())
                        .userId(user.getId())
                        .startTime(s.getCreatedAt())
                        .lastActivity(s.getEndedAt() != null ? s.getEndedAt() : s.getCreatedAt())
                        .messageCount(Math.toIntExact(messageRepository.countByChatSession(s)))
                        .crisisDetectionCount((int) messageRepository.findByChatSessionOrderByCreatedAt(s).stream().filter(m -> m.getCrisisLevel() != null && m.getCrisisLevel() != CrisisLevel.NONE).count())
                        .isActive(s.getIsActive())
                        .sessionSummary("")
                        .build()
        ).toList();
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

        // 추가적인 위기 상황 알림 처리 (이메일, SMS, 관리자 알림 등)
        if (crisisLevel == CrisisLevel.HIGH) {
            // 긴급 상황 알림 로직 추가
            log.error("긴급 위기 상황 발생 - 사용자: {}, 세션: {}", user.getId(), sessionId);
            // TODO: 관리자 알림, 응급 연락처 알림 등
        }
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