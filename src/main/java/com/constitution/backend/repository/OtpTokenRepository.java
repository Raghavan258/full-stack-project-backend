package com.constitution.backend.repository;

import com.constitution.backend.entity.OtpToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findTopByEmailAndUsedFalseOrderByCreatedAtDesc(String email);

    @Modifying
    @Transactional
    @Query("DELETE FROM OtpToken o WHERE o.expiresAt < :now OR o.used = true")
    void deleteExpiredAndUsed(LocalDateTime now);
}
