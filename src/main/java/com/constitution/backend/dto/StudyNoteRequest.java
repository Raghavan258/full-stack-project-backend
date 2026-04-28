package com.constitution.backend.dto;

import com.constitution.backend.entity.StudyNote;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class StudyNoteRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;

    private String category;

    private String videoUrl;

    private StudyNote.Status status = StudyNote.Status.Published;

    private StudyNote.ApprovalStatus approvalStatus;
}
