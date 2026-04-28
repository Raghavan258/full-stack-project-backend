package com.constitution.backend.repository;

import com.constitution.backend.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByActiveTrue();
    List<QuizQuestion> findByCategoryAndActiveTrue(String category);
    
    @Query("SELECT DISTINCT q.category FROM QuizQuestion q WHERE q.active = true AND q.category IS NOT NULL")
    List<String> findActiveCategories();
}
