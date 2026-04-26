package com.mkrasikoff.epigraph.service;

import com.mkrasikoff.epigraph.exception.QuoteNotFoundException;
import com.mkrasikoff.epigraph.model.Quote;
import com.mkrasikoff.epigraph.repository.QuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    /**
     * Returns the Quote of the Day for a user — deterministic, date-based.
     * Uses quotes sorted by ID (creation order) so adding new quotes doesn't
     * shift today's selection.
     */
    @Transactional(readOnly = true)
    public Optional<Quote> getQod(Long userId) {
        List<Quote> sorted = repo.findByUserId(userId).stream()
                .sorted(Comparator.comparingLong(Quote::getId))
                .toList();

        if (sorted.isEmpty()) return Optional.empty();

        // Same deterministic hash as the frontend (date string → int)
        String today = java.time.LocalDate.now(java.time.ZoneId.of("Europe/Moscow")).toString(); // "2026-04-25"
        int hash = 0;
        for (char c : today.toCharArray()) hash = 31 * hash + c;

        return Optional.of(sorted.get(Math.abs(hash) % sorted.size()));
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
        repo.deleteAllByUserId(userId);
    }

    /**
     * Creates 3 onboarding instruction quotes for a newly registered user.
     * Called right after registration (local and OAuth2).
     */
    @Transactional
    public void createDefaultQuotes(Long userId) {
        long now = System.currentTimeMillis();

        List<Quote> defaults = List.of(
                buildDefaultQuote(
                        "⭐ Отмечайте любимые цитаты звёздочкой — они попадут в избранное. " +
                                "На вкладке «На сегодня» каждый день вас ждёт одна из ваших цитат. Удалите эти карточки, когда освоитесь.",
                        "Epigraph", null, "инструкция", now + 2),
                buildDefaultQuote(
                        "➕ Чтобы добавить цитату, перейдите в раздел «Добавить». " +
                                "Укажите текст, автора, источник и теги — это поможет находить нужное через поиск.",
                        "Epigraph", null, "инструкция", now + 1),
                buildDefaultQuote(
                        "👋 Добро пожаловать в Epigraph! Это ваше личное хранилище цитат. " +
                                "Сохраняйте фразы, которые вас вдохновляют, удивляют или заставляют думать.",
                        "Epigraph", null, "инструкция", now)
        );

        defaults.forEach(q -> {
            q.setUserId(userId);
            repo.save(q);
        });
    }

    private Quote buildDefaultQuote(String text, String author, String source, String tags, long added) {
        Quote q = new Quote();

        q.setText(text);
        q.setAuthor(author);
        q.setSource(source);
        q.setTags(tags);
        q.setAdded(added);

        return q;
    }
}
