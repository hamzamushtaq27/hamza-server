package com.dgsw.hamza.service;

import com.dgsw.hamza.dto.ChatDto;
import com.dgsw.hamza.entity.ChatMessage;
import com.dgsw.hamza.entity.ChatSession;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.MessageType;
import com.dgsw.hamza.enums.UserRole;
import com.dgsw.hamza.repository.ChatMessageRepository;
import com.dgsw.hamza.repository.ChatSessionRepository;
import com.dgsw.hamza.util.CrisisDetector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatService 테스트")
class ChatServiceTest {

    @Mock
    private ChatSessionRepository chatSessionRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private WebClient webClient;

    @Mock
    private CrisisDetector crisisDetector;

    @InjectMocks
    private ChatService chatService;

    private User testUser;
    private ChatSession testSession;
    private ChatMessage testMessage;
    private ChatDto.ChatMessageRequest messageRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        testSession = ChatSession.builder()
                .id(1L)
                .user(testUser)
                .sessionName("테스트 세션")
                .isActive(true)
                .startTime(LocalDateTime.now())
                .build();

        testMessage = ChatMessage.builder()
                .id(1L)
                .session(testSession)
                .content("안녕하세요")
                .messageType(MessageType.USER)
                .timestamp(LocalDateTime.now())
                .build();

        messageRequest = ChatDto.ChatMessageRequest.builder()
                .message("안녕하세요")
                .build();

        // WebClient 설정
        ReflectionTestUtils.setField(chatService, "openaiApiKey", "test-api-key");
    }

    @Test
    @DisplayName("활성 세션 조회 성공")
    void getActiveSession_Success() {
        // given
        given(chatSessionRepository.findByUserAndIsActiveTrue(testUser)).willReturn(Optional.of(testSession));

        // when
        ChatDto.ChatSessionResponse response = chatService.getActiveSession(testUser);

        // then
        assertThat(response.getSessionId()).isEqualTo(1L);
        assertThat(response.getSessionName()).isEqualTo("테스트 세션");
        assertThat(response.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("활성 세션 없음 - 새 세션 생성")
    void getActiveSession_CreateNew() {
        // given
        given(chatSessionRepository.findByUserAndIsActiveTrue(testUser)).willReturn(Optional.empty());
        given(chatSessionRepository.save(any(ChatSession.class))).willReturn(testSession);

        // when
        ChatDto.ChatSessionResponse response = chatService.getActiveSession(testUser);

        // then
        assertThat(response.getSessionId()).isEqualTo(1L);
        assertThat(response.getIsActive()).isTrue();
        verify(chatSessionRepository, times(1)).save(any(ChatSession.class));
    }

    @Test
    @DisplayName("채팅 히스토리 조회 성공")
    void getChatHistory_Success() {
        // given
        List<ChatMessage> messages = Arrays.asList(
                createMessage(1L, "안녕하세요", MessageType.USER),
                createMessage(2L, "안녕하세요! 무엇을 도와드릴까요?", MessageType.ASSISTANT)
        );

        given(chatSessionRepository.findByUserAndIsActiveTrue(testUser)).willReturn(Optional.of(testSession));
        given(chatMessageRepository.findBySessionOrderByTimestampAsc(testSession)).willReturn(messages);

        // when
        ChatDto.ChatHistoryResponse response = chatService.getChatHistory(testUser);

        // then
        assertThat(response.getSessionId()).isEqualTo(1L);
        assertThat(response.getMessages()).hasSize(2);
        assertThat(response.getMessages().get(0).getContent()).isEqualTo("안녕하세요");
        assertThat(response.getMessages().get(1).getContent()).isEqualTo("안녕하세요! 무엇을 도와드릴까요?");
    }

    @Test
    @DisplayName("위기 상황 감지 테스트")
    void detectCrisis_Success() {
        // given
        String crisisMessage = "죽고 싶어요";
        CrisisDetector.CrisisLevel crisisLevel = CrisisDetector.CrisisLevel.CRITICAL;
        
        given(crisisDetector.detectCrisis(anyString())).willReturn(crisisLevel);

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(crisisMessage);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.CRITICAL);
        verify(crisisDetector, times(1)).detectCrisis(crisisMessage);
    }

    @Test
    @DisplayName("일반 메시지 처리 테스트")
    void processNormalMessage_Success() {
        // given
        String normalMessage = "오늘 기분이 좋아요";
        CrisisDetector.CrisisLevel normalLevel = CrisisDetector.CrisisLevel.NONE;
        
        given(crisisDetector.detectCrisis(anyString())).willReturn(normalLevel);

        // when
        CrisisDetector.CrisisLevel result = crisisDetector.detectCrisis(normalMessage);

        // then
        assertThat(result).isEqualTo(CrisisDetector.CrisisLevel.NONE);
        verify(crisisDetector, times(1)).detectCrisis(normalMessage);
    }

    @Test
    @DisplayName("챗봇 상태 확인 테스트")
    void getChatbotStatus_Success() {
        // when
        ChatDto.ChatbotStatusResponse response = chatService.getChatbotStatus();

        // then
        assertThat(response.getIsAvailable()).isTrue();
        assertThat(response.getStatus()).isEqualTo("ACTIVE");
        assertThat(response.getVersion()).isEqualTo("1.0.0");
    }

    private ChatMessage createMessage(Long id, String content, MessageType messageType) {
        return ChatMessage.builder()
                .id(id)
                .session(testSession)
                .content(content)
                .messageType(messageType)
                .timestamp(LocalDateTime.now())
                .build();
    }
}