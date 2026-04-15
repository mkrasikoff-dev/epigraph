package com.mkrasikoff.epigraph.service;

import com.mkrasikoff.epigraph.exception.QuoteNotFoundException;
import com.mkrasikoff.epigraph.model.Quote;
import com.mkrasikoff.epigraph.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class QuoteService {

    private final QuoteRepository repo;

    public QuoteService(QuoteRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<Quote> findAll() {
        return repo.findAll();
    }

    @Transactional
    public Quote save(Quote quote) {
        if (quote.getAdded() == null) {
            quote.setAdded(System.currentTimeMillis());
        }

        return repo.save(quote);
    }

    @Transactional
    public Quote update(Long id, Quote incoming) {
        Quote existing = repo.findById(id).orElseThrow(() -> new QuoteNotFoundException(id));

        existing.setText(incoming.getText());
        existing.setAuthor(incoming.getAuthor());
        existing.setSource(incoming.getSource());
        existing.setTags(incoming.getTags());
        existing.setFav(incoming.isFav());

        return repo.save(existing);
    }

    @Transactional
    public void deleteById(Long id) {
        if (!repo.existsById(id)) throw new QuoteNotFoundException(id);
        repo.deleteById(id);
    }

    @Transactional
    public void deleteAll() {
        repo.deleteAll();
    }
}
