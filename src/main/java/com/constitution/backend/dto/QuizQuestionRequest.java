package com.constitution.backend.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class QuizQuestionRequest {

    @NotBlank(message = "Question text is required")
    private String question;

    @NotBlank(message = "Option A is required")
    private String optionA;

    @NotBlank(message = "Option B is required")
    private String optionB;

    @NotBlank(message = "Option C is required")
    private String optionC;

    @NotBlank(message = "Option D is required")
    private String optionD;

    @Min(value = 0, message = "Correct option must be 0–3")
    @Max(value = 3, message = "Correct option must be 0–3")
    private int correctOption;

    private String category;
}
