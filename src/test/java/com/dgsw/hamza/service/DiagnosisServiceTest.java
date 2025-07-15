package com.dgsw.hamza.service;

import com.dgsw.hamza.dto.DiagnosisDto;
import com.dgsw.hamza.entity.Diagnosis;
import com.dgsw.hamza.entity.User;
import com.dgsw.hamza.enums.DiagnosisSeverity;
import com.dgsw.hamza.enums.UserRole;
import com.dgsw.hamza.repository.DiagnosisRepository;
import com.dgsw.hamza.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiagnosisService 테스트")
class DiagnosisServiceTest {

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DiagnosisService diagnosisService;

    private User testUser;
    private DiagnosisDto.DiagnosisSubmitRequest submitRequest;
    private Diagnosis testDiagnosis;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .nickname("testuser")
                .password("encodedPassword")
                .role(UserRole.USER)
                .build();

        submitRequest = DiagnosisDto.DiagnosisSubmitRequest.builder()
                .answers(Arrays.asList(2, 1, 3, 2, 1, 2, 1, 2, 1)) // 총 15점
                .build();

        testDiagnosis = Diagnosis.builder()
                .id(1L)
                .user(testUser)
                .totalScore(15)
                .severity(DiagnosisSeverity.MODERATE)
                .answers("2,1,3,2,1,2,1,2,1")
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("PHQ-9 설문 문항 조회 성공")
    void getQuestions_Success() {
        // when
        List<DiagnosisDto.QuestionResponse> questions = diagnosisService.getQuestions();

        // then
        assertThat(questions).hasSize(9);
        assertThat(questions.get(0).getQuestionId()).isEqualTo(1L);
        assertThat(questions.get(0).getQuestionText()).contains("흥미나 즐거움");
        assertThat(questions.get(0).getQuestionOrder()).isEqualTo(1);
        assertThat(questions.get(0).getIsActive()).isTrue();
    }

    @Test
    @DisplayName("진단 제출 성공 - 중등도 우울증")
    void submitDiagnosis_Success_Moderate() {
        // given
        given(diagnosisRepository.save(any(Diagnosis.class))).willReturn(testDiagnosis);

        // when
        DiagnosisDto.DiagnosisResponse response = diagnosisService.submitDiagnosis(testUser, submitRequest);

        // then
        assertThat(response.getTotalScore()).isEqualTo(15);
        assertThat(response.getSeverity()).isEqualTo(DiagnosisSeverity.MODERATE);
        assertThat(response.getRecommendation()).contains("전문가 상담");
        verify(diagnosisRepository, times(1)).save(any(Diagnosis.class));
    }

    @Test
    @DisplayName("진단 제출 성공 - 정상 범위")
    void submitDiagnosis_Success_Normal() {
        // given
        DiagnosisDto.DiagnosisSubmitRequest normalRequest = DiagnosisDto.DiagnosisSubmitRequest.builder()
                .answers(Arrays.asList(0, 0, 1, 0, 0, 1, 0, 0, 0)) // 총 2점
                .build();

        Diagnosis normalDiagnosis = Diagnosis.builder()
                .id(1L)
                .user(testUser)
                .totalScore(2)
                .severity(DiagnosisSeverity.NORMAL)
                .answers("0,0,1,0,0,1,0,0,0")
                .createdAt(LocalDateTime.now())
                .build();

        given(diagnosisRepository.save(any(Diagnosis.class))).willReturn(normalDiagnosis);

        // when
        DiagnosisDto.DiagnosisResponse response = diagnosisService.submitDiagnosis(testUser, normalRequest);

        // then
        assertThat(response.getTotalScore()).isEqualTo(2);
        assertThat(response.getSeverity()).isEqualTo(DiagnosisSeverity.NORMAL);
        assertThat(response.getRecommendation()).contains("건강한 상태");
        verify(diagnosisRepository, times(1)).save(any(Diagnosis.class));
    }

    @Test
    @DisplayName("진단 제출 성공 - 심각한 우울증")
    void submitDiagnosis_Success_Severe() {
        // given
        DiagnosisDto.DiagnosisSubmitRequest severeRequest = DiagnosisDto.DiagnosisSubmitRequest.builder()
                .answers(Arrays.asList(3, 3, 3, 3, 3, 3, 3, 3, 3)) // 총 27점
                .build();

        Diagnosis severeDiagnosis = Diagnosis.builder()
                .id(1L)
                .user(testUser)
                .totalScore(27)
                .severity(DiagnosisSeverity.VERY_SEVERE)
                .answers("3,3,3,3,3,3,3,3,3")
                .createdAt(LocalDateTime.now())
                .build();

        given(diagnosisRepository.save(any(Diagnosis.class))).willReturn(severeDiagnosis);

        // when
        DiagnosisDto.DiagnosisResponse response = diagnosisService.submitDiagnosis(testUser, severeRequest);

        // then
        assertThat(response.getTotalScore()).isEqualTo(27);
        assertThat(response.getSeverity()).isEqualTo(DiagnosisSeverity.VERY_SEVERE);
        assertThat(response.getRecommendation()).contains("즉시 전문의");
        verify(diagnosisRepository, times(1)).save(any(Diagnosis.class));
    }

    @Test
    @DisplayName("진단 통계 조회 성공")
    void getDiagnosisStats_Success() {
        // given
        List<Diagnosis> diagnoses = Arrays.asList(
                createDiagnosis(1L, 10, DiagnosisSeverity.MILD),
                createDiagnosis(2L, 15, DiagnosisSeverity.MODERATE),
                createDiagnosis(3L, 20, DiagnosisSeverity.SEVERE)
        );

        given(diagnosisRepository.findTop10ByUserOrderByCreatedAtDesc(testUser)).willReturn(diagnoses);

        // when
        DiagnosisDto.DiagnosisStatsResponse response = diagnosisService.getDiagnosisStats(testUser);

        // then
        assertThat(response.getTotalDiagnoses()).isEqualTo(3);
        assertThat(response.getAverageScore()).isEqualTo(15.0);
        assertThat(response.getLatestSeverity()).isEqualTo(DiagnosisSeverity.MILD);
        assertThat(response.getScoreHistory()).hasSize(3);
    }

    private Diagnosis createDiagnosis(Long id, int score, DiagnosisSeverity severity) {
        return Diagnosis.builder()
                .id(id)
                .user(testUser)
                .totalScore(score)
                .severity(severity)
                .answers("1,1,1,1,1,1,1,1,1")
                .createdAt(LocalDateTime.now())
                .build();
    }
}