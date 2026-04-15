package com.mkrasikoff.epigraph.repository;

import com.mkrasikoff.epigraph.model.Quote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuoteRepository extends JpaRepository<Quote, Long> {
}
