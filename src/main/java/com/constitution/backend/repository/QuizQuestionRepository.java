package com.constitution.backend.repository;

import com.constitution.backend.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByActiveTrue();
    List<QuizQuestion> findByCategoryAndActiveTrue(String category);
}
