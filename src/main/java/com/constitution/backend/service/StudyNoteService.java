package com.constitution.backend.service;

import com.constitution.backend.dto.StudyNoteRequest;
import com.constitution.backend.entity.StudyNote;
import com.constitution.backend.entity.User;
import com.constitution.backend.exception.ApiException;
import com.constitution.backend.repository.StudyNoteRepository;
import com.constitution.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyNoteService {

    private final StudyNoteRepository noteRepository;
    private final UserRepository userRepository;

    public Page<StudyNote> getPublished(int page, int size) {
        // Only return APPROVED notes to public
        return noteRepository.findByStatusAndApprovalStatus(
                StudyNote.Status.Published,
                StudyNote.ApprovalStatus.APPROVED,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Page<StudyNote> search(String keyword, int page, int size) {
        return noteRepository.findByCategoryContainingIgnoreCaseOrTitleContainingIgnoreCase(
                keyword, keyword, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public StudyNote getById(Long id) {
        return noteRepository.findById(id)
                .orElseThrow(() -> new ApiException("Study note not found", HttpStatus.NOT_FOUND));
    }

    public List<StudyNote> getPendingApproval() {
        return noteRepository.findByApprovalStatus(StudyNote.ApprovalStatus.PENDING_REVIEW);
    }

    public List<StudyNote> getByAuthor(String email) {
        User author = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        return noteRepository.findByAuthor(author);
    }

    @Transactional
    public StudyNote create(StudyNoteRequest req, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        StudyNote note = StudyNote.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .category(req.getCategory())
                .videoUrl(req.getVideoUrl())
                .status(req.getStatus())
                .approvalStatus(StudyNote.ApprovalStatus.PENDING_REVIEW)
                .author(author)
                .build();
        return noteRepository.save(note);
    }

    @Transactional
    public StudyNote update(Long id, StudyNoteRequest req, String editorEmail) {
        StudyNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ApiException("Study note not found", HttpStatus.NOT_FOUND));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        boolean isAdmin = editor.getRole() == User.Role.admin;
        boolean isAuthor = note.getAuthor().getId().equals(editor.getId());
        if (!isAdmin && !isAuthor) {
            throw new ApiException("Not authorized to edit this note", HttpStatus.FORBIDDEN);
        }

        note.setTitle(req.getTitle());
        note.setContent(req.getContent());
        note.setCategory(req.getCategory());
        note.setVideoUrl(req.getVideoUrl());
        note.setStatus(req.getStatus());
        // Re-submit for review if edited by non-admin
        if (!isAdmin) {
            note.setApprovalStatus(StudyNote.ApprovalStatus.PENDING_REVIEW);
        }
        return noteRepository.save(note);
    }

    @Transactional
    public StudyNote approve(Long id) {
        StudyNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ApiException("Study note not found", HttpStatus.NOT_FOUND));
        note.setApprovalStatus(StudyNote.ApprovalStatus.APPROVED);
        note.setRejectionReason(null);
        return noteRepository.save(note);
    }

    @Transactional
    public StudyNote reject(Long id, String reason) {
        StudyNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ApiException("Study note not found", HttpStatus.NOT_FOUND));
        note.setApprovalStatus(StudyNote.ApprovalStatus.REJECTED);
        note.setRejectionReason(reason);
        return noteRepository.save(note);
    }

    @Transactional
    public StudyNote updateVideoUrl(Long id, String videoUrl) {
        StudyNote note = noteRepository.findById(id)
                .orElseThrow(() -> new ApiException("Study note not found", HttpStatus.NOT_FOUND));
        note.setVideoUrl(videoUrl);
        return noteRepository.save(note);
    }

    @Transactional
    public void delete(Long id) {
        if (!noteRepository.existsById(id)) {
            throw new ApiException("Study note not found", HttpStatus.NOT_FOUND);
        }
        noteRepository.deleteById(id);
    }
}
