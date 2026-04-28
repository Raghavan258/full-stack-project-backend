package com.constitution.backend.dto;

import com.constitution.backend.entity.Article;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// ─── Request ───────────────────────────────────────────────────────────────
@Data
public class ArticleRequest {

    @NotBlank(message = "Part is required")
    @Size(max = 50)
    private String part;

    @NotBlank(message = "Title is required")
    @Size(max = 200)
    private String title;

    @NotBlank(message = "Summary is required")
    private String summary;

    private String fullContent;
    private String tags;
    private Article.Status status = Article.Status.Draft;
}

// ─── Response ──────────────────────────────────────────────────────────────
// (defined as inner static class for simplicity; can be split if needed)
class ArticleResponse {
    // mapped in service
}
