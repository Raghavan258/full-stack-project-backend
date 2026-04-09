package com.constitution.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Column(nullable = false, length = 500)
    private String optionA;
    @Column(nullable = false, length = 500)
    private String optionB;
    @Column(nullable = false, length = 500)
    private String optionC;
    @Column(nullable = false, length = 500)
    private String optionD;

    // 0=A, 1=B, 2=C, 3=D
    @Column(nullable = false)
    private int correctOption;

    @Column(length = 100)
    private String category;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
