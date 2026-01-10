package com.elearnhub.teacher_service.repository;

import com.elearnhub.teacher_service.entity.EmailVerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {
    
    // Find token by token string
    Optional<EmailVerificationToken> findByToken(String token);
    
    // Find tokens by user ID
    List<EmailVerificationToken> findByUserId(Long userId);
    
    // Find tokens by email
    List<EmailVerificationToken> findByEmail(String email);
    
    // Find valid (unused and not expired) token by user ID
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.userId = :userId AND t.used = false AND t.expiresAt > :now")
    Optional<EmailVerificationToken> findValidTokenByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // Find valid token by email
    @Query("SELECT t FROM EmailVerificationToken t WHERE t.email = :email AND t.used = false AND t.expiresAt > :now")
    Optional<EmailVerificationToken> findValidTokenByEmail(@Param("email") String email, @Param("now") LocalDateTime now);
    
    // Delete expired tokens
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.expiresAt < :now")
    void deleteExpiredTokens(@Param("now") LocalDateTime now);
    
    // Delete used tokens older than specified date
    @Modifying
    @Query("DELETE FROM EmailVerificationToken t WHERE t.used = true AND t.usedAt < :cutoffDate")
    void deleteOldUsedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    // Count valid tokens for user
    @Query("SELECT COUNT(t) FROM EmailVerificationToken t WHERE t.userId = :userId AND t.used = false AND t.expiresAt > :now")
    long countValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    // Check if user has any valid tokens
    @Query("SELECT COUNT(t) > 0 FROM EmailVerificationToken t WHERE t.userId = :userId AND t.used = false AND t.expiresAt > :now")
    boolean hasValidTokens(@Param("userId") Long userId, @Param("now") LocalDateTime now);
}