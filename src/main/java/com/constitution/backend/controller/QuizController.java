package com.constitution.backend.controller;

import com.constitution.backend.dto.ApiResponse;
import com.constitution.backend.dto.QuizQuestionRequest;
import com.constitution.backend.entity.QuizQuestion;
import com.constitution.backend.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "Quiz questions and scoring")
public class QuizController {

    private final QuizService quizService;

    @GetMapping
    @Operation(summary = "Get all active quiz questions (optional category filter)")
    public ResponseEntity<ApiResponse<List<QuizQuestion>>> getAll(
            @RequestParam(required = false) String category) {
        List<QuizQuestion> questions = (category != null && !category.isBlank())
                ? quizService.getByCategory(category)
                : quizService.getAll();
        return ResponseEntity.ok(ApiResponse.ok("Questions retrieved", questions));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a quiz question by ID")
    public ResponseEntity<ApiResponse<QuizQuestion>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Question found", quizService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EDUCATOR','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a quiz question (Educator / Admin)")
    public ResponseEntity<ApiResponse<QuizQuestion>> create(
            @Valid @RequestBody QuizQuestionRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        QuizQuestion q = quizService.create(req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Question created", q));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDUCATOR','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update a quiz question (Educator / Admin)")
    public ResponseEntity<ApiResponse<QuizQuestion>> update(
            @PathVariable Long id,
            @Valid @RequestBody QuizQuestionRequest req) {
        QuizQuestion q = quizService.update(id, req);
        return ResponseEntity.ok(ApiResponse.ok("Question updated", q));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Soft-delete a quiz question (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        quizService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Question deleted"));
    }

    /**
     * Submit quiz answers and get score.
     * Body: { "1": 2, "2": 0, "5": 3 }  (questionId -> selectedOption 0-3)
     */
    @PostMapping("/submit")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Submit quiz answers and receive score",
               description = "Send a JSON map of { questionId: selectedOption (0-3) }")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> submit(
            @RequestBody Map<Long, Integer> answers) {
        int score = quizService.scoreQuiz(answers);
        return ResponseEntity.ok(ApiResponse.ok(
                "Quiz scored",
                Map.of("score", score, "total", answers.size())));
    }
}
