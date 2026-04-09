package com.constitution.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "forum_replies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumReply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "thread_id", nullable = false)
    private ForumThread thread;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String body;

    @Column(nullable = false)
    @Builder.Default
    private int likes = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
