package com.constitution.backend.service;

import com.constitution.backend.entity.Article;
import com.constitution.backend.entity.Query;
import com.constitution.backend.entity.StudyNote;
import com.constitution.backend.entity.User;
import com.constitution.backend.exception.ApiException;
import com.constitution.backend.repository.ArticleRepository;
import com.constitution.backend.repository.ForumThreadRepository;
import com.constitution.backend.repository.QueryRepository;
import com.constitution.backend.repository.StudyNoteRepository;
import com.constitution.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final ForumThreadRepository threadRepository;
    private final StudyNoteRepository studyNoteRepository;
    private final QueryRepository queryRepository;

    public Map<String, Object> getPlatformStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("verifiedUsers", userRepository.countVerified());
        stats.put("citizens", userRepository.countByRole(User.Role.citizen));
        stats.put("educators", userRepository.countByRole(User.Role.educator));
        stats.put("legalExperts", userRepository.countByRole(User.Role.legal_expert));
        stats.put("admins", userRepository.countByRole(User.Role.admin));
        stats.put("totalArticles", articleRepository.count());
        stats.put("publishedArticles", articleRepository.countByStatus(Article.Status.Published));
        stats.put("flaggedArticles", articleRepository.countByFlaggedTrue());
        stats.put("totalThreads", threadRepository.count());
        stats.put("totalStudyNotes", studyNoteRepository.count());
        stats.put("pendingStudyNotes", studyNoteRepository.countByApprovalStatus(StudyNote.ApprovalStatus.PENDING_REVIEW));
        stats.put("approvedStudyNotes", studyNoteRepository.countByApprovalStatus(StudyNote.ApprovalStatus.APPROVED));
        stats.put("totalQueries", queryRepository.count());
        stats.put("pendingQueries", queryRepository.countByStatus(Query.QueryStatus.PENDING));
        stats.put("answeredQueries", queryRepository.countByStatus(Query.QueryStatus.ANSWERED));
        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsersByRole(User.Role role) {
        return userRepository.findByRole(role);
    }

    @Transactional
    public User toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        user.setActive(!user.isActive());
        return userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ApiException("User not found", HttpStatus.NOT_FOUND);
        }
        userRepository.deleteById(userId);
    }

    @Transactional
    public User updateUserRole(Long userId, User.Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));
        user.setRole(newRole);
        return userRepository.save(user);
    }

    public List<Query> getAllQueries() {
        return queryRepository.findAll();
    }

    public List<StudyNote> getPendingStudyNotes() {
        return studyNoteRepository.findByApprovalStatus(StudyNote.ApprovalStatus.PENDING_REVIEW);
    }
}
