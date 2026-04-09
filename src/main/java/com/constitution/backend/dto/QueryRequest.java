package com.constitution.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryRequest {
    @NotBlank
    private String question;
    private Long assignedToId;
}
