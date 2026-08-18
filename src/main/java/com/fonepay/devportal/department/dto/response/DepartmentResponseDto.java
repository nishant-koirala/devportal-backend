package com.fonepay.devportal.department.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDto {

    private String departmentId;
    private String departmentName;
    private String departmentDescription;
    private boolean isActive;
    private Instant createdAt;
}
