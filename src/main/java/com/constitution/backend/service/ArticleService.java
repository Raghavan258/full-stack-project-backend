package com.constitution.backend.service;

import com.constitution.backend.dto.ArticleRequest;
import com.constitution.backend.entity.Article;
import com.constitution.backend.entity.User;
import com.constitution.backend.exception.ApiException;
import com.constitution.backend.repository.ArticleRepository;
import com.constitution.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public Page<Article> getPublished(int page, int size) {
        return articleRepository.findByStatus(
                Article.Status.Published,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Page<Article> search(String keyword, int page, int size) {
        return articleRepository.findByTitleContainingIgnoreCaseOrSummaryContainingIgnoreCase(
                keyword, keyword, PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public Article getById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));
        articleRepository.incrementViews(id);
        return article;
    }

    public List<Article> getFlagged() {
        return articleRepository.findByFlaggedTrue();
    }

    @Transactional
    public Article create(ArticleRequest req, String authorEmail) {
        User author = userRepository.findByEmail(authorEmail)
                .orElseThrow(() -> new ApiException("Author not found", HttpStatus.NOT_FOUND));

        Article article = Article.builder()
                .part(req.getPart())
                .title(req.getTitle())
                .summary(req.getSummary())
                .fullContent(req.getFullContent())
                .tags(req.getTags())
                .status(req.getStatus())
                .author(author)
                .build();
        return articleRepository.save(article);
    }

    @Transactional
    public Article update(Long id, ArticleRequest req, String editorEmail) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));

        User editor = userRepository.findByEmail(editorEmail)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

        boolean isAdmin = editor.getRole() == User.Role.admin;
        boolean isAuthor = article.getAuthor() != null &&
                article.getAuthor().getId().equals(editor.getId());
        if (!isAdmin && !isAuthor) {
            throw new ApiException("You are not authorized to edit this article", HttpStatus.FORBIDDEN);
        }

        article.setPart(req.getPart());
        article.setTitle(req.getTitle());
        article.setSummary(req.getSummary());
        article.setFullContent(req.getFullContent());
        article.setTags(req.getTags());
        article.setStatus(req.getStatus());
        return articleRepository.save(article);
    }

    @Transactional
    public Article saveLegalCommentary(Long id, String commentary) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));
        article.setLegalCommentary(commentary);
        return articleRepository.save(article);
    }

    @Transactional
    public Article flagArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));
        article.setFlagged(true);
        return articleRepository.save(article);
    }

    @Transactional
    public Article unflagArticle(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ApiException("Article not found", HttpStatus.NOT_FOUND));
        article.setFlagged(false);
        return articleRepository.save(article);
    }

    @Transactional
    public void delete(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ApiException("Article not found", HttpStatus.NOT_FOUND);
        }
        articleRepository.deleteById(id);
    }

    public Page<Article> getAll(int page, int size) {
        return articleRepository.findAll(
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }
}
