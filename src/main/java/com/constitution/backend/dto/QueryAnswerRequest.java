package com.constitution.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryAnswerRequest {
    @NotBlank
    private String answer;
}
