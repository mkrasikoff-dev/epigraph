package com.mkrasikoff.epigraph.controller;

import com.mkrasikoff.epigraph.model.Quote;
import com.mkrasikoff.epigraph.repository.QuoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private static final Logger log = LoggerFactory.getLogger(QuoteController.class);

    @Autowired
    private QuoteRepository repo;

    @GetMapping
    public List<Quote> getAll() {
        List<Quote> quotes = repo.findAll();

        log.info("GET /api/quotes - returning {} quotes", quotes.size());

        return quotes;
    }

    @PostMapping
    public Quote create(@RequestBody Quote q) {
        if (q.getAdded() == null) {
            q.setAdded(System.currentTimeMillis());
        }

        Quote saved = repo.save(q);

        log.info("POST /api/quotes - created quote id = {}, author = '{}'", saved.getId(), saved.getAuthor());

        return saved;
    }

    @PutMapping("/{id}")
    public Quote update(@PathVariable Long id, @RequestBody Quote q) {
        q.setId(id);
        Quote updated = repo.save(q);

        log.info("PUT /api/quotes/{} - updated quote, fav = {}", id, updated.isFav());

        return updated;
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);

        log.info("DELETE /api/quotes/{} - deleted", id);
    }

    @DeleteMapping
    public void deleteAll() {
        repo.deleteAll();

        log.warn("DELETE /api/quotes - ALL quotes deleted");
    }
}
