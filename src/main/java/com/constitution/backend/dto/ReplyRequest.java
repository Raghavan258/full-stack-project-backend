package com.constitution.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReplyRequest {

    @NotBlank(message = "Reply body is required")
    private String body;
}
