package com.example.til.repository;

import com.example.til.domain.User;
import com.example.til.domain.VerificationToken;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from VerificationToken v where v.user = :user or v.expiresAt < :now")
    int deleteByUserOrExpired(@Param("user") User user, @Param("now") LocalDateTime now);
}