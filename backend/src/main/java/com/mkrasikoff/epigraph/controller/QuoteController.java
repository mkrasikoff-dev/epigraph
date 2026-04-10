package com.mkrasikoff.epigraph.controller;

import com.mkrasikoff.epigraph.model.Quote;
import com.mkrasikoff.epigraph.repository.QuoteRepository;
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

    @Autowired
    private QuoteRepository repo;

    @GetMapping
    public List<Quote> getAll() { return repo.findAll(); }

    @PostMapping
    public Quote create(@RequestBody Quote q) {
        if (q.getAdded() == null) q.setAdded(System.currentTimeMillis());
        return repo.save(q);
    }

    @PutMapping("/{id}")
    public Quote update(@PathVariable Long id, @RequestBody Quote q) {
        q.setId(id);
        return repo.save(q);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) { repo.deleteById(id); }

    @DeleteMapping
    public void deleteAll() { repo.deleteAll(); }
}
