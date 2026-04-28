package com.constitution.backend.service;

import com.constitution.backend.dto.ReplyRequest;
import com.constitution.backend.dto.ThreadRequest;
import com.constitution.backend.entity.ForumReply;
import com.constitution.backend.entity.ForumThread;
import com.constitution.backend.entity.User;
import com.constitution.backend.exception.ApiException;
import com.constitution.backend.repository.ForumReplyRepository;
import com.constitution.backend.repository.ForumThreadRepository;
import com.constitution.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumService {

    private final ForumThreadRepository threadRepository;
    private final ForumReplyRepository replyRepository;
    private final UserRepository userRepository;

    public Page<ForumThread> getAll(int page, int size) {
        return threadRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Page<ForumThread> getByTopic(String topic, int page, int size) {
        return threadRepository.findByTopic(
                topic, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public ForumThread getById(Long id) {
        return threadRepository.findById(id)
                .orElseThrow(() -> new ApiException("Thread not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public ForumThread createThread(ThreadRequest req, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        ForumThread thread = ForumThread.builder()
                .topic(req.getTopic())
                .title(req.getTitle())
                .body(req.getBody())
                .author(author)
                .build();
        return threadRepository.save(thread);
    }

    @Transactional
    public ForumThread updateThread(Long id, ThreadRequest req, String editorEmail) {
        ForumThread thread = threadRepository.findById(id)
                .orElseThrow(() -> new ApiException("Thread not found", HttpStatus.NOT_FOUND));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        boolean isAdmin = editor.getRole() == User.Role.admin;
        boolean isAuthor = thread.getAuthor().getId().equals(editor.getId());
        if (!isAdmin && !isAuthor) {
            throw new ApiException("Not authorized to edit this thread", HttpStatus.FORBIDDEN);
        }

        thread.setTopic(req.getTopic());
        thread.setTitle(req.getTitle());
        thread.setBody(req.getBody());
        return threadRepository.save(thread);
    }

    @Transactional
    public void deleteThread(Long id, String requesterEmail) {
        ForumThread thread = threadRepository.findById(id)
                .orElseThrow(() -> new ApiException("Thread not found", HttpStatus.NOT_FOUND));

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        boolean isAdmin = requester.getRole() == User.Role.admin;
        boolean isAuthor = thread.getAuthor().getId().equals(requester.getId());
        if (!isAdmin && !isAuthor) {
            throw new ApiException("Not authorized to delete this thread", HttpStatus.FORBIDDEN);
        }
        threadRepository.deleteById(id);
    }

    @Transactional
    public void likeThread(Long id) {
        if (!threadRepository.existsById(id)) {
            throw new ApiException("Thread not found", HttpStatus.NOT_FOUND);
        }
        threadRepository.incrementLikes(id);
    }

    // ── Replies ─────────────────────────────────────────────────────────────

    public List<ForumReply> getReplies(Long threadId) {
        if (!threadRepository.existsById(threadId)) {
            throw new ApiException("Thread not found", HttpStatus.NOT_FOUND);
        }
        return replyRepository.findByThreadId(threadId);
    }

    @Transactional
    public ForumReply addReply(Long threadId, ReplyRequest req, String authorEmail) {
        ForumThread thread = threadRepository.findById(threadId)
                .orElseThrow(() -> new ApiException("Thread not found", HttpStatus.NOT_FOUND));
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        ForumReply reply = ForumReply.builder()
                .thread(thread)
                .author(author)
                .body(req.getBody())
                .build();
        return replyRepository.save(reply);
    }

    @Transactional
    public void deleteReply(Long replyId, String requesterEmail) {
        ForumReply reply = replyRepository.findById(replyId)
                .orElseThrow(() -> new ApiException("Reply not found", HttpStatus.NOT_FOUND));
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        boolean isAdmin = requester.getRole() == User.Role.admin;
        boolean isAuthor = reply.getAuthor().getId().equals(requester.getId());
        if (!isAdmin && !isAuthor) {
            throw new ApiException("Not authorized to delete this reply", HttpStatus.FORBIDDEN);
        }
        replyRepository.deleteById(replyId);
    }

    @Transactional
    public void likeReply(Long replyId) {
        if (!replyRepository.existsById(replyId)) {
            throw new ApiException("Reply not found", HttpStatus.NOT_FOUND);
        }
        replyRepository.incrementLikes(replyId);
    }
}
