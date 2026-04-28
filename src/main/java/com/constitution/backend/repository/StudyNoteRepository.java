package com.constitution.backend.repository;

import com.constitution.backend.entity.StudyNote;
import com.constitution.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudyNoteRepository extends JpaRepository<StudyNote, Long> {
    Page<StudyNote> findByStatus(StudyNote.Status status, Pageable pageable);
    Page<StudyNote> findByStatusAndApprovalStatus(StudyNote.Status status, StudyNote.ApprovalStatus approvalStatus, Pageable pageable);
    Page<StudyNote> findByCategoryContainingIgnoreCaseOrTitleContainingIgnoreCase(String cat, String title, Pageable pageable);
    List<StudyNote> findByApprovalStatus(StudyNote.ApprovalStatus approvalStatus);
    List<StudyNote> findByAuthor(User author);
    long countByApprovalStatus(StudyNote.ApprovalStatus approvalStatus);
}
