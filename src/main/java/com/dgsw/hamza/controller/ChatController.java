package com.dgsw.hamza.controller;

import com.dgsw.hamza.dto.ChatDto;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.CrisisLevel;
import com.dgsw.hamza.repository.UserRepository;
import com.dgsw.hamza.security.UserPrincipal;
import com.dgsw.hamza.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Chat", description = "AI 챗봇 대화 API")
public class ChatController {

    private final ChatService chatService;
    private final UserRepository userRepository;

    @Operation(summary = "채팅 메시지 전송", description = "사용자 메시지를 전송하고 AI 챗봇 응답을 받습니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "메시지 전송 성공",
                    content = @Content(schema = @Schema(implementation = ChatDto.ChatMessageResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/message")
    public ResponseEntity<ChatDto.ChatMessageResponse> sendMessage(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChatDto.ChatMessageRequest request) {

        log.info("사용자 {} 메시지 전송: {}", userPrincipal.getId(), request.getMessage());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatDto.ChatMessageResponse response = chatService.processMessage(user, request);

        // 위기 상황 감지 시 알림 처리
        if (response.getCrisisDetected() != null && response.getCrisisDetected()) {
            chatService.handleCrisisAlert(user, response.getSessionId(), response.getCrisisLevel());
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채팅 히스토리 조회", description = "사용자의 채팅 기록을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "히스토리 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/history")
    public ResponseEntity<ChatDto.ChatHistoryResponse> getChatHistory(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody ChatDto.ChatHistoryRequest request) {

        log.info("사용자 {} 채팅 히스토리 조회", userPrincipal.getId());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ChatDto.ChatHistoryResponse response = chatService.getChatHistory(user, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "활성 세션 조회", description = "사용자의 현재 활성 세션들을 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "활성 세션 조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/sessions/active")
    public ResponseEntity<List<ChatDto.ChatSessionInfo>> getActiveSessions(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        log.info("사용자 {} 활성 세션 조회", userPrincipal.getId());

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<ChatDto.ChatSessionInfo> sessions = chatService.getActiveSessions(user);

        return ResponseEntity.ok(sessions);
    }

    @Operation(summary = "세션 종료", description = "특정 채팅 세션을 종료합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "세션 종료 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "세션을 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "세션 접근 권한 없음"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/sessions/{sessionId}/end")
    public ResponseEntity<Void> endSession(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "세션 ID") @PathVariable String sessionId) {

        log.info("사용자 {} 세션 {} 종료", userPrincipal.getId(), sessionId);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        chatService.endSession(user, sessionId);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "챗봇 상태 조회", description = "챗봇 시스템의 현재 상태를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "챗봇 상태 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/status")
    public ResponseEntity<ChatDto.ChatbotStatus> getChatbotStatus() {
        log.info("챗봇 상태 조회");

        ChatDto.ChatbotStatus status = chatService.getChatbotStatus();

        return ResponseEntity.ok(status);
    }

    @Operation(summary = "감정 분석 보고서", description = "사용자의 감정 분석 보고서를 생성합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "감정 분석 보고서 생성 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/emotion-report")
    public ResponseEntity<Object> getEmotionReport(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "분석 기간 (일)") @RequestParam(defaultValue = "30") int days) {

        log.info("사용자 {} 감정 분석 보고서 생성 - {}일", userPrincipal.getId(), days);

        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Object report = chatService.generateEmotionReport(user, days);

        return ResponseEntity.ok(report);
    }

    @Operation(summary = "위기 상황 대응 가이드", description = "위기 상황 발생 시 대응 방법을 안내합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "위기 대응 가이드 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/crisis-guide")
    public ResponseEntity<Object> getCrisisGuide(
            @Parameter(description = "위기 수준") @RequestParam(required = false) CrisisLevel level) {

        log.info("위기 상황 대응 가이드 조회 - 수준: {}", level);

        Object guide = new Object() {
            public final String title = "위기 상황 대응 가이드";
            public final Object emergencyContacts = new Object() {
                public final String lifeline = "생명의전화: 1588-9191 (24시간)";
                public final String youthHotline = "청소년전화: 1388";
                public final String mentalHealthCrisis = "정신건강위기상담: 1577-0199";
                public final String emergency = "응급실: 119";
                public final String police = "경찰신고: 112";
            };
            public final Object crisisLevels = new Object() {
                public final String CRITICAL = "즉각 조치 필요 - 119 신고 또는 응급실 방문";
                public final String HIGH = "높은 위험 - 즉시 전문가 상담";
                public final String MEDIUM = "중간 위험 - 전문가 상담 권장";
                public final String LOW = "낮은 위험 - 자기 관리 및 관찰";
                public final String NONE = "정상 상태 - 건강 관리 지속";
            };
            public final List<String> immediateActions = List.of(
                    "안전한 곳으로 이동하기",
                    "믿을 만한 사람에게 연락하기",
                    "전문가 도움 요청하기",
                    "혼자 있지 않기",
                    "위험한 물건 제거하기"
            );
            public final List<String> supportResources = List.of(
                    "정신건강복지센터",
                    "자살예방센터",
                    "병원 응급실",
                    "온라인 상담 서비스",
                    "지역 상담센터"
            );
        };

        return ResponseEntity.ok(guide);
    }

    @Operation(summary = "챗봇 사용법 안내", description = "AI 챗봇 사용 방법을 안내합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용법 안내 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/help")
    public ResponseEntity<Object> getChatHelp() {
        log.info("챗봇 사용법 안내 조회");

        Object help = new Object() {
            public final String title = "AI 챗봇 사용 가이드";
            public final List<String> features = List.of(
                    "24시간 언제든지 대화 가능",
                    "위기 상황 자동 감지 및 대응",
                    "감정 분석 및 피드백",
                    "개인화된 치료법 추천",
                    "전문가 연결 서비스"
            );
            public final List<String> usageTips = List.of(
                    "솔직하고 자연스럽게 대화하세요",
                    "감정이나 상황을 구체적으로 표현하세요",
                    "위기 상황에서는 즉시 도움을 요청하세요",
                    "정기적인 대화로 감정 상태를 관리하세요",
                    "필요시 전문가 상담을 받으세요"
            );
            public final List<String> limitations = List.of(
                    "전문 의료진을 대체할 수 없습니다",
                    "응급상황에서는 119에 신고하세요",
                    "약물 처방이나 진단은 할 수 없습니다",
                    "개인정보는 안전하게 보호됩니다"
            );
            public final Object sampleQuestions = new Object() {
                public final List<String> general = List.of(
                        "안녕하세요",
                        "오늘 기분이 좋지 않아요",
                        "스트레스를 받고 있어요",
                        "잠을 잘 못 자겠어요"
                );
                public final List<String> crisis = List.of(
                        "도움이 필요해요",
                        "상담받고 싶어요",
                        "병원을 찾아주세요",
                        "응급상황이에요"
                );
            };
        };

        return ResponseEntity.ok(help);
    }

    @Operation(summary = "개인정보 보호 정책", description = "챗봇 서비스의 개인정보 보호 정책을 안내합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "개인정보 보호 정책 조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/privacy")
    public ResponseEntity<Object> getPrivacyPolicy() {
        log.info("개인정보 보호 정책 조회");

        Object privacy = new Object() {
            public final String title = "개인정보 보호 정책";
            public final List<String> dataProtection = List.of(
                    "모든 대화 내용은 암호화되어 저장됩니다",
                    "개인정보는 치료 목적으로만 사용됩니다",
                    "사용자 동의 없이 제3자에게 제공되지 않습니다",
                    "위기 상황에서만 응급 서비스와 공유됩니다"
            );
            public final List<String> dataRetention = List.of(
                    "대화 기록은 치료 연속성을 위해 보관됩니다",
                    "사용자 요청 시 언제든 삭제 가능합니다",
                    "법적 요구사항에 따라 일정 기간 보관됩니다",
                    "비활성 계정은 자동으로 삭제됩니다"
            );
            public final List<String> userRights = List.of(
                    "개인정보 열람 및 수정 권리",
                    "개인정보 삭제 요청 권리",
                    "개인정보 처리 중단 요청 권리",
                    "개인정보 이용 내역 통지 요구 권리"
            );
            public final String contact = "개인정보 관련 문의: privacy@hamza.com";
        };

        return ResponseEntity.ok(privacy);
    }
}