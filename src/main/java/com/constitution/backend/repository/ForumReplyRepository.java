package com.constitution.backend.repository;

import com.constitution.backend.entity.ForumReply;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ForumReplyRepository extends JpaRepository<ForumReply, Long> {
    List<ForumReply> findByThreadId(Long threadId);

    @Modifying
    @Transactional
    @Query("UPDATE ForumReply r SET r.likes = r.likes + 1 WHERE r.id = :id")
    void incrementLikes(Long id);
}
