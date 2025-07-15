package com.dgsw.hamza.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PageRequest(
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    int page,
    
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100 이하여야 합니다")
    int size,
    
    String sort,
    
    String direction
) {
    public PageRequest {
        if (page < 0) page = 0;
        if (size < 1) size = 20;
        if (size > 100) size = 100;
        if (sort == null || sort.isBlank()) sort = "id";
        if (direction == null || direction.isBlank()) direction = "desc";
    }
    
    public org.springframework.data.domain.PageRequest toPageable() {
        org.springframework.data.domain.Sort.Direction dir = 
            "asc".equalsIgnoreCase(direction) ? 
                org.springframework.data.domain.Sort.Direction.ASC : 
                org.springframework.data.domain.Sort.Direction.DESC;
        
        return org.springframework.data.domain.PageRequest.of(
            page, size, org.springframework.data.domain.Sort.by(dir, sort)
        );
    }
}