package com.dgsw.hamza.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record HospitalSearchRequest(
    @NotNull(message = "위도는 필수입니다")
    @DecimalMin(value = "33.0", message = "위도는 33.0 이상이어야 합니다")
    @DecimalMax(value = "38.0", message = "위도는 38.0 이하여야 합니다")
    Double latitude,
    
    @NotNull(message = "경도는 필수입니다")
    @DecimalMin(value = "124.0", message = "경도는 124.0 이상이어야 합니다")
    @DecimalMax(value = "132.0", message = "경도는 132.0 이하여야 합니다")
    Double longitude,
    
    @Min(value = 1, message = "반경은 1km 이상이어야 합니다")
    @Max(value = 50, message = "반경은 50km 이하여야 합니다")
    Integer radius
) {}