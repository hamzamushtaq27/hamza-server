package com.dgsw.hamza.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DiagnosisRequest(
    @NotNull(message = "답변 목록은 필수입니다")
    @Size(min = 9, max = 9, message = "PHQ-9는 정확히 9개의 답변이 필요합니다")
    List<@Min(value = 0, message = "답변 점수는 0 이상이어야 합니다") 
         @Max(value = 3, message = "답변 점수는 3 이하여야 합니다") 
         Integer> answers
) {}