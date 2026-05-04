package com.mkrasikoff.epigraph.repository;

import com.mkrasikoff.epigraph.model.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    /** Finds the most recent unused, unexpired code for the given email. */
    @Query("""
        SELECT v FROM EmailVerification v
        WHERE v.email = :email
          AND v.used = false
          AND v.expiresAt > :now
        ORDER BY v.createdAt DESC
        LIMIT 1
    """)
    Optional<EmailVerification> findValidCode(String email, long now);

    /** Marks all previous codes for an email as used before issuing a new one. */
    @Modifying
    @Query("UPDATE EmailVerification v SET v.used = true WHERE v.email = :email")
    void invalidateAll(String email);
}
