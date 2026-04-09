package com.constitution.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forum_threads")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ForumThread {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String topic;   // e.g. Preamble, Rights

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Builder.Default
    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ForumReply> replies = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private int likes = 0;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
