package com.constitution.backend.controller;

import com.constitution.backend.dto.ApiResponse;
import com.constitution.backend.entity.Query;
import com.constitution.backend.entity.StudyNote;
import com.constitution.backend.entity.User;
import com.constitution.backend.service.AdminService;
import com.constitution.backend.service.StudyNoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "BearerAuth")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin-only platform management endpoints")
public class AdminController {

    private final AdminService adminService;
    private final StudyNoteService studyNoteService;

    @GetMapping("/stats")
    @Operation(summary = "Get platform statistics (user counts, content counts, query stats)")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok("Platform stats", adminService.getPlatformStats()));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users (optional role filter)")
    public ResponseEntity<ApiResponse<List<User>>> getUsers(
            @RequestParam(required = false) User.Role role) {
        List<User> users = (role != null)
                ? adminService.getUsersByRole(role)
                : adminService.getAllUsers();
        return ResponseEntity.ok(ApiResponse.ok("Users retrieved", users));
    }

    @PatchMapping("/users/{id}/toggle-active")
    @Operation(summary = "Toggle user active/inactive status")
    public ResponseEntity<ApiResponse<User>> toggleActive(@PathVariable Long id) {
        User user = adminService.toggleUserActive(id);
        return ResponseEntity.ok(ApiResponse.ok("User status updated", user));
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Update a user's role")
    public ResponseEntity<ApiResponse<User>> updateRole(
            @PathVariable Long id,
            @RequestParam User.Role role) {
        User user = adminService.updateUserRole(id, role);
        return ResponseEntity.ok(ApiResponse.ok("User role updated", user));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Permanently delete a user account")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.ok("User deleted"));
    }

    @GetMapping("/queries")
    @Operation(summary = "Get all queries with full user details")
    public ResponseEntity<ApiResponse<List<Query>>> getAllQueries() {
        return ResponseEntity.ok(ApiResponse.ok("All queries", adminService.getAllQueries()));
    }

    @GetMapping("/study-notes/pending")
    @Operation(summary = "Get all study notes pending approval")
    public ResponseEntity<ApiResponse<List<StudyNote>>> getPendingNotes() {
        return ResponseEntity.ok(ApiResponse.ok("Pending study notes", adminService.getPendingStudyNotes()));
    }

    @PostMapping("/study-notes/{id}/approve")
    @Operation(summary = "Approve a study note")
    public ResponseEntity<ApiResponse<StudyNote>> approveNote(@PathVariable Long id) {
        StudyNote note = studyNoteService.approve(id);
        return ResponseEntity.ok(ApiResponse.ok("Study note approved", note));
    }

    @PostMapping("/study-notes/{id}/reject")
    @Operation(summary = "Reject a study note with optional reason")
    public ResponseEntity<ApiResponse<StudyNote>> rejectNote(
            @PathVariable Long id,
            @RequestParam(required = false, defaultValue = "") String reason) {
        StudyNote note = studyNoteService.reject(id, reason);
        return ResponseEntity.ok(ApiResponse.ok("Study note rejected", note));
    }
}
