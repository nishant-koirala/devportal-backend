package com.fonepay.devportal.modules.department.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "departments")
public class Department {

    @Id
    private String departmentId;

    @Field("department_name")
    private String departmentName; 

    @Field("department_description")
    private String departmentDescription;

    @Field("is_active")
    private boolean isActive; 

    @Field("created_at")
    private Instant createdAt; 
}
