package com.constitution.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// ─── Thread request ────────────────────────────────────────────────────────
@Data
public class ThreadRequest {

    @NotBlank(message = "Topic is required")
    @Size(max = 50)
    private String topic;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 300, message = "Title must be 5–300 characters")
    private String title;

    private String body;
}
