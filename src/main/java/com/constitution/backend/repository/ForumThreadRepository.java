package com.constitution.backend.repository;

import com.constitution.backend.entity.ForumThread;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ForumThreadRepository extends JpaRepository<ForumThread, Long> {
    Page<ForumThread> findByTopic(String topic, Pageable pageable);
    Page<ForumThread> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE ForumThread t SET t.likes = t.likes + 1 WHERE t.id = :id")
    void incrementLikes(Long id);
}
