package com.constitution.backend.repository;

import com.constitution.backend.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByStatus(Article.Status status, Pageable pageable);
    List<Article> findByPart(String part);
    Page<Article> findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(String titleKw, String summaryKw, Pageable pageable);
    List<Article> findByFlaggedTrue();
    long countByFlaggedTrue();
    long countByStatus(Article.Status status);

    @Modifying
    @Transactional
    @Query("UPDATE Article a SET a.views = a.views + 1 WHERE a.id = :id")
    void incrementViews(Long id);
}
