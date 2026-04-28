package com.constitution.backend.controller;

import com.constitution.backend.dto.ApiResponse;
import com.constitution.backend.dto.ArticleRequest;
import com.constitution.backend.entity.Article;
import com.constitution.backend.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
@Tag(name = "Articles", description = "CRUD for constitutional articles")
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping
    @Operation(summary = "Get all published articles (paginated)")
    public ResponseEntity<ApiResponse<Page<Article>>> getPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Page<Article> result = (search != null && !search.isBlank())
                ? articleService.search(search, page, size)
                : articleService.getPublished(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Articles retrieved", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get article by ID (also increments view count)")
    public ResponseEntity<ApiResponse<Article>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Article found", articleService.getById(id)));
    }

    @GetMapping("/flagged")
    @PreAuthorize("hasAnyRole('ADMIN','LEGAL_EXPERT')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get all flagged articles (Admin / Legal Expert)")
    public ResponseEntity<ApiResponse<List<Article>>> getFlagged() {
        return ResponseEntity.ok(ApiResponse.ok("Flagged articles", articleService.getFlagged()));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get all articles including drafts (Admin only)")
    public ResponseEntity<ApiResponse<Page<Article>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.ok("All articles", articleService.getAll(page, size)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EDUCATOR','LEGAL_EXPERT','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a new article (Educator / Legal Expert / Admin)")
    public ResponseEntity<ApiResponse<Article>> create(
            @Valid @RequestBody ArticleRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        Article created = articleService.create(req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Article created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDUCATOR','LEGAL_EXPERT','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update an article (author or Admin only)")
    public ResponseEntity<ApiResponse<Article>> update(
            @PathVariable Long id,
            @Valid @RequestBody ArticleRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        Article updated = articleService.update(id, req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Article updated", updated));
    }

    @PatchMapping("/{id}/legal-commentary")
    @PreAuthorize("hasAnyRole('LEGAL_EXPERT','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Save or update legal commentary on an article (Legal Expert / Admin)")
    public ResponseEntity<ApiResponse<Article>> saveLegalCommentary(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Article article = articleService.saveLegalCommentary(id, body.get("legalCommentary"));
        return ResponseEntity.ok(ApiResponse.ok("Legal commentary saved", article));
    }

    @PatchMapping("/{id}/flag")
    @PreAuthorize("hasAnyRole('LEGAL_EXPERT','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Flag an article for review (Legal Expert / Admin)")
    public ResponseEntity<ApiResponse<Article>> flag(@PathVariable Long id) {
        Article article = articleService.flagArticle(id);
        return ResponseEntity.ok(ApiResponse.ok("Article flagged", article));
    }

    @PatchMapping("/{id}/unflag")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Unflag an article (Admin only)")
    public ResponseEntity<ApiResponse<Article>> unflag(@PathVariable Long id) {
        Article article = articleService.unflagArticle(id);
        return ResponseEntity.ok(ApiResponse.ok("Article unflagged", article));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete an article (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Article deleted"));
    }
}
