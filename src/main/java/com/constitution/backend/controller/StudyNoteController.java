package com.constitution.backend.controller;

import com.constitution.backend.dto.ApiResponse;
import com.constitution.backend.dto.StudyNoteRequest;
import com.constitution.backend.entity.StudyNote;
import com.constitution.backend.service.StudyNoteService;
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
@RequestMapping("/api/study-notes")
@RequiredArgsConstructor
@Tag(name = "Study Notes", description = "Educational study notes for the Constitution")
public class StudyNoteController {

    private final StudyNoteService noteService;

    @GetMapping
    @Operation(summary = "Get all approved published study notes (paginated, optional search)")
    public ResponseEntity<ApiResponse<Page<StudyNote>>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search) {

        Page<StudyNote> result = (search != null && !search.isBlank())
                ? noteService.search(search, page, size)
                : noteService.getPublished(page, size);
        return ResponseEntity.ok(ApiResponse.ok("Study notes retrieved", result));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get study note by ID")
    public ResponseEntity<ApiResponse<StudyNote>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("Note found", noteService.getById(id)));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('EDUCATOR','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get my own study notes (Educator / Admin)")
    public ResponseEntity<ApiResponse<List<StudyNote>>> getMine(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<StudyNote> notes = noteService.getByAuthor(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Your study notes", notes));
    }

    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Get all study notes pending approval (Admin only)")
    public ResponseEntity<ApiResponse<List<StudyNote>>> getPending() {
        List<StudyNote> notes = noteService.getPendingApproval();
        return ResponseEntity.ok(ApiResponse.ok("Pending study notes", notes));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('EDUCATOR','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Create a study note (Educator / Admin) — goes to PENDING_REVIEW")
    public ResponseEntity<ApiResponse<StudyNote>> create(
            @Valid @RequestBody StudyNoteRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyNote note = noteService.create(req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Study note submitted for review", note));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDUCATOR','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update a study note (author or Admin)")
    public ResponseEntity<ApiResponse<StudyNote>> update(
            @PathVariable Long id,
            @Valid @RequestBody StudyNoteRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        StudyNote updated = noteService.update(id, req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Study note updated", updated));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Approve a study note (Admin only)")
    public ResponseEntity<ApiResponse<StudyNote>> approve(@PathVariable Long id) {
        StudyNote note = noteService.approve(id);
        return ResponseEntity.ok(ApiResponse.ok("Study note approved", note));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Reject a study note with optional reason (Admin only)")
    public ResponseEntity<ApiResponse<StudyNote>> reject(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String reason) {
        StudyNote note = noteService.reject(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Study note rejected", note));
    }

    @PatchMapping("/{id}/video")
    @PreAuthorize("hasAnyRole('EDUCATOR','ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Update video URL for a study note (Educator / Admin)")
    public ResponseEntity<ApiResponse<StudyNote>> updateVideo(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        StudyNote note = noteService.updateVideoUrl(id, body.get("videoUrl"));
        return ResponseEntity.ok(ApiResponse.ok("Video URL updated", note));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "BearerAuth")
    @Operation(summary = "Delete a study note (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        noteService.delete(id);
        return ResponseEntity.ok(ApiResponse.ok("Study note deleted"));
    }
}
