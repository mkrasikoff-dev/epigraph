package com.mkrasikoff.epigraph.repository;

import com.mkrasikoff.epigraph.model.PushSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {

    List<PushSubscription> findByUserId(Long userId);

    Optional<PushSubscription> findByEndpoint(String endpoint);

    @Modifying
    @Transactional
    void deleteByEndpointAndUserId(String endpoint, Long userId);

    /**
     * Subscriptions where (last_sent + interval) <= now — i.e. overdue.
     */
    @Query(
            value = "SELECT * FROM push_subscriptions " +
                    "WHERE last_sent + (interval_hours::bigint * 3600000) <= :now",
            nativeQuery = true
    )
    List<PushSubscription> findDue(@Param("now") long now);
}
