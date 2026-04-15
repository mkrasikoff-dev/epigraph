package com.mkrasikoff.epigraph.exception;

public class QuoteNotFoundException extends RuntimeException {

    public QuoteNotFoundException(Long id) {
        super("Quote not found: " + id);
    }
}
