package com.constitution.backend.service;

import com.constitution.backend.dto.QuizQuestionRequest;
import com.constitution.backend.entity.QuizQuestion;
import com.constitution.backend.entity.User;
import com.constitution.backend.exception.ApiException;
import com.constitution.backend.repository.QuizQuestionRepository;
import com.constitution.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizQuestionRepository quizRepo;
    private final UserRepository userRepository;

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

    /** Score a submitted quiz: map of questionId -> selectedOption (0-3) */
    public int scoreQuiz(java.util.Map<Long, Integer> answers) {
        int score = 0;
        for (java.util.Map.Entry<Long, Integer> entry : answers.entrySet()) {
            QuizQuestion q = quizRepo.findById(entry.getKey()).orElse(null);
            if (q != null && q.getCorrectOption() == entry.getValue()) {
                score++;
            }
        }
        return score;
    }
}
