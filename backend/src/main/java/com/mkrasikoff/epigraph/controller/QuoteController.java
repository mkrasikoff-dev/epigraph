package com.mkrasikoff.epigraph.controller;

import com.mkrasikoff.epigraph.model.Quote;
import com.mkrasikoff.epigraph.service.QuoteService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public List<Quote> getAll(@AuthenticationPrincipal Long userId) {
        List<Quote> quotes = service.findAll(userId);

        log.info("Fetching all quotes — found {}", quotes.size());

        return quotes;
    }

    @GetMapping("/qod")
    public ResponseEntity<Quote> getQod(@AuthenticationPrincipal Long userId) {
        return service.getQod(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Quote create(@Valid @RequestBody Quote quote,
                        @AuthenticationPrincipal Long userId) {
        Quote saved = service.save(quote, userId);

        log.info("Quote created — id = {}", saved.getId());

        return saved;
    }

    @PutMapping("/{id}")
    public Quote update(@PathVariable Long id,
                        @Valid @RequestBody Quote quote,
                        @AuthenticationPrincipal Long userId) {
        Quote updated = service.update(id, quote, userId);

        log.info("Quote updated — id = {}, fav = {}", id, updated.isFav());

        return updated;
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal Long userId) {
        service.deleteById(id, userId);

        log.info("Quote deleted — id = {}", id);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(@AuthenticationPrincipal Long userId) {
        service.deleteAll(userId);

        log.info("All quotes deleted for userId = {}", userId);
    }
}
