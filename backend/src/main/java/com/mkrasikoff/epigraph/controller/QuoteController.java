package com.mkrasikoff.epigraph.controller;

import com.mkrasikoff.epigraph.model.Quote;
import com.mkrasikoff.epigraph.service.QuoteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/quotes")
public class QuoteController {

    private static final Logger log = LoggerFactory.getLogger(QuoteController.class);

    private final QuoteService service;

    public QuoteController(QuoteService service) {
        this.service = service;
    }

    @GetMapping
    public List<Quote> getAll() {
        List<Quote> quotes = service.findAll();

        log.info("GET /api/quotes - returning {} quotes", quotes.size());

        return quotes;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Quote create(@Valid @RequestBody Quote quote) {
        Quote saved = service.save(quote);

        log.info("POST /api/quotes - created quote id = {}, author = '{}'", saved.getId(), saved.getAuthor());

        return saved;
    }

    @PutMapping("/{id}")
    public Quote update(@PathVariable Long id, @Valid @RequestBody Quote quote) {
        Quote updated = service.update(id, quote);

        log.info("PUT /api/quotes/{} - updated quote, fav = {}", id, updated.isFav());

        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.deleteById(id);

        log.info("DELETE /api/quotes/{} - deleted", id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll() {
        service.deleteAll();

        log.info("DELETE /api/quotes - ALL quotes deleted");
    }
}
