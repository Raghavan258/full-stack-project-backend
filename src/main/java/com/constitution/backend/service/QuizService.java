package com.constitution.backend.service;

import com.constitution.backend.dto.QuizQuestionRequest;
import com.constitution.backend.entity.QuizAttempt;
import com.constitution.backend.entity.QuizQuestion;
import com.constitution.backend.entity.User;
import com.constitution.backend.exception.ApiException;
import com.constitution.backend.repository.QuizAttemptRepository;
import com.constitution.backend.repository.QuizQuestionRepository;
import com.constitution.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizQuestionRepository quizRepo;
    private final UserRepository userRepository;
    private final QuizAttemptRepository attemptRepo;

    public List<QuizQuestion> getAll() {
        return quizRepo.findByActiveTrue();
    }

    public List<QuizQuestion> getByCategory(String category) {
        return quizRepo.findByCategoryAndActiveTrue(category);
    }

    public QuizQuestion getById(Long id) {
        return quizRepo.findById(id)
                .orElseThrow(() -> new ApiException("Question not found", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public QuizQuestion create(QuizQuestionRequest req, String creatorEmail) {
        User creator = userRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        QuizQuestion q = QuizQuestion.builder()
                .question(req.getQuestion())
                .optionA(req.getOptionA())
                .optionB(req.getOptionB())
                .optionC(req.getOptionC())
                .optionD(req.getOptionD())
                .correctOption(req.getCorrectOption())
                .category(req.getCategory())
                .createdBy(creator)
                .build();
        return quizRepo.save(q);
    }

    @Transactional
    public QuizQuestion update(Long id, QuizQuestionRequest req) {
        QuizQuestion q = quizRepo.findById(id)
                .orElseThrow(() -> new ApiException("Question not found", HttpStatus.NOT_FOUND));

        q.setQuestion(req.getQuestion());
        q.setOptionA(req.getOptionA());
        q.setOptionB(req.getOptionB());
        q.setOptionC(req.getOptionC());
        q.setOptionD(req.getOptionD());
        q.setCorrectOption(req.getCorrectOption());
        q.setCategory(req.getCategory());
        return quizRepo.save(q);
    }

    @Transactional
    public void delete(Long id) {
        QuizQuestion q = quizRepo.findById(id)
                .orElseThrow(() -> new ApiException("Question not found", HttpStatus.NOT_FOUND));
        q.setActive(false); // soft delete
        quizRepo.save(q);
    }

    public List<QuizQuestion> generateMixedQuiz(int countPerCategory) {
        List<String> categories = quizRepo.findActiveCategories();
        List<QuizQuestion> mixedQuiz = new ArrayList<>();
        
        for (String cat : categories) {
            List<QuizQuestion> categoryQuestions = quizRepo.findByCategoryAndActiveTrue(cat);
            Collections.shuffle(categoryQuestions);
            mixedQuiz.addAll(categoryQuestions.stream().limit(countPerCategory).toList());
        }
        Collections.shuffle(mixedQuiz);
        return mixedQuiz;
    }

    /** Score a submitted quiz: map of questionId -> selectedOption (0-3) */
    @Transactional
    public int scoreQuiz(java.util.Map<Long, Integer> answers, String userEmail) {
        int score = 0;
        for (java.util.Map.Entry<Long, Integer> entry : answers.entrySet()) {
            QuizQuestion q = quizRepo.findById(entry.getKey()).orElse(null);
            if (q != null && q.getCorrectOption() == entry.getValue()) {
                score++;
            }
        }
        
        if (userEmail != null) {
            User user = userRepository.findByEmail(userEmail).orElse(null);
            if (user != null) {
                QuizAttempt attempt = QuizAttempt.builder()
                        .user(user)
                        .score(score)
                        .totalQuestions(answers.size())
                        .build();
                attemptRepo.save(attempt);
            }
        }
        return score;
    }
}
