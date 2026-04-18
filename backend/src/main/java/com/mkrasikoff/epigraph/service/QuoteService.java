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
    public List<Quote> findAll(Long userId) {
        return repo.findByUserId(userId);
    }

    @Transactional
    public Quote save(Quote quote, Long userId) {
        if (quote.getAdded() == null) {
            quote.setAdded(System.currentTimeMillis());
        }

        quote.setUserId(userId);

        return repo.save(quote);
    }

    @Transactional
    public Quote update(Long id, Quote incoming, Long userId) {
        Quote existing = repo.findByIdAndUserId(id, userId).orElseThrow(() -> new QuoteNotFoundException(id));

        existing.setText(incoming.getText());
        existing.setAuthor(incoming.getAuthor());
        existing.setSource(incoming.getSource());
        existing.setTags(incoming.getTags());
        existing.setFav(incoming.isFav());

        return repo.save(existing);
    }

    @Transactional
    public void deleteById(Long id, Long userId) {
        if (!repo.existsByIdAndUserId(id, userId)) throw new QuoteNotFoundException(id);

        repo.deleteByIdAndUserId(id, userId);
    }

    @Transactional
    public void deleteAll(Long userId) {
        repo.findByUserId(userId).forEach(q -> repo.deleteById(q.getId()));
    }
}
