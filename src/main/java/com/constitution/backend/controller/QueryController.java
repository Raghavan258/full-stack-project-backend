package com.constitution.backend.controller;

import com.constitution.backend.dto.ApiResponse;
import com.constitution.backend.dto.QueryAnswerRequest;
import com.constitution.backend.dto.QueryRequest;
import com.constitution.backend.entity.Query;
import com.constitution.backend.service.QueryService;
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

@RestController
@RequestMapping("/api/queries")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
@Tag(name = "Queries", description = "Citizen query submission and expert answer workflow")
public class QueryController {

    private final QueryService queryService;

    @PostMapping
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Submit a new query (Citizen only)")
    public ResponseEntity<ApiResponse<Query>> submit(
            @Valid @RequestBody QueryRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        Query query = queryService.createQuery(req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Query submitted", query));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CITIZEN')")
    @Operation(summary = "Get own submitted queries with answers (Citizen only)")
    public ResponseEntity<ApiResponse<List<Query>>> getMine(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Query> queries = queryService.getMyCitizenQueries(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Your queries", queries));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EDUCATOR','LEGAL_EXPERT','ADMIN')")
    @Operation(summary = "Get queries assigned to me or pending (Educator / Legal Expert / Admin)")
    public ResponseEntity<ApiResponse<List<Query>>> getAll(
            @RequestParam(required = false, defaultValue = "false") boolean assignedToMe,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Query> queries = assignedToMe
                ? queryService.getForResponder(userDetails.getUsername())
                : queryService.getAllQueries();
        return ResponseEntity.ok(ApiResponse.ok("Queries retrieved", queries));
    }

    @PatchMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN','EDUCATOR','LEGAL_EXPERT')")
    @Operation(summary = "Assign query to a user (Admin or self-assign by educator/legal_expert)")
    public ResponseEntity<ApiResponse<Query>> assign(
            @PathVariable Long id,
            @RequestParam Long userId) {
        Query query = queryService.assignQuery(id, userId);
        return ResponseEntity.ok(ApiResponse.ok("Query assigned", query));
    }

    @PatchMapping("/{id}/answer")
    @PreAuthorize("hasAnyRole('EDUCATOR','LEGAL_EXPERT')")
    @Operation(summary = "Answer a query (Educator / Legal Expert)")
    public ResponseEntity<ApiResponse<Query>> answer(
            @PathVariable Long id,
            @Valid @RequestBody QueryAnswerRequest req,
            @AuthenticationPrincipal UserDetails userDetails) {
        Query query = queryService.answerQuery(id, req, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.ok("Query answered", query));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close a query (Admin only)")
    public ResponseEntity<ApiResponse<Query>> close(@PathVariable Long id) {
        Query query = queryService.closeQuery(id);
        return ResponseEntity.ok(ApiResponse.ok("Query closed", query));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a query (Admin only)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        queryService.deleteQuery(id);
        return ResponseEntity.ok(ApiResponse.ok("Query deleted"));
    }
}
