package com.constitution.backend.controller;

import com.constitution.backend.dto.ApiResponse;
import com.constitution.backend.dto.ReplyRequest;
import com.constitution.backend.dto.ThreadRequest;
import com.constitution.backend.entity.ForumReply;
import com.constitution.backend.entity.ForumThread;
import com.constitution.backend.service.ForumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forums")
@RequiredArgsConstructor
@Tag(name = "Forums", description = "Discussion threads and replies")
public class ForumController {

    private final ForumService forumService;

    @GetMapping
    @Operation(summary = "Get all forum threads (paginated, optional topic filter)")
    public ResponseEntity<ApiResponse<Page<ForumThread>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String topic) {

        Page<ForumThread> result = (topic != null && !topic.isBlank())
                ? forumService.getByTopic(topic, page, size)
                : forumService.getAll(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Threads retrieved", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a forum thread by ID")
    public ResponseEntity<ApiResponse<ForumThread>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Thread found", forumService.getById(id)));
    }

    @PostMapping
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a new discussion thread")
    public ResponseEntity<ApiResponse<ForumThread>> create(
            @Valid @RequestBody ThreadRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        ForumThread thread = forumService.createThread(req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Thread created", thread));
    }

    @PutMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update a thread (author or Admin)")
    public ResponseEntity<ApiResponse<ForumThread>> update(
            @PathVariable Long id,
            @Valid @RequestBody ThreadRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        ForumThread updated = forumService.updateThread(id, req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Thread updated", updated));
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete a thread (author or Admin)")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        forumService.deleteThread(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Thread deleted"));
    }

    @PostMapping("/{id}/like")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Like a thread")
    public ResponseEntity<ApiResponse<Void>> likeThread(@PathVariable Long id) {
        forumService.likeThread(id);
        return ResponseEntity.ok(ApiResponse.ok("Thread liked"));
    }

    // ── Replies ─────────────────────────────────────────────────────────────

    @GetMapping("/{threadId}/replies")
    @Operation(summary = "Get all replies for a thread")
    public ResponseEntity<ApiResponse<List<ForumReply>>> getReplies(@PathVariable Long threadId) {
        return ResponseEntity.ok(ApiResponse.ok("Replies retrieved", forumService.getReplies(threadId)));
    }

    @PostMapping("/{threadId}/replies")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Add a reply to a thread")
    public ResponseEntity<ApiResponse<ForumReply>> addReply(
            @PathVariable Long threadId,
            @Valid @RequestBody ReplyRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        ForumReply reply = forumService.addReply(threadId, req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Reply added", reply));
    }

    @DeleteMapping("/replies/{replyId}")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete a reply (author or Admin)")
    public ResponseEntity<ApiResponse<Void>> deleteReply(
            @PathVariable Long replyId,
            @AuthenticationPrincipal UserDetails userDetails) {
        forumService.deleteReply(replyId, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Reply deleted"));
    }

    @PostMapping("/replies/{replyId}/like")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Like a reply")
    public ResponseEntity<ApiResponse<Void>> likeReply(@PathVariable Long replyId) {
        forumService.likeReply(replyId);
        return ResponseEntity.ok(ApiResponse.ok("Reply liked"));
    }
}
