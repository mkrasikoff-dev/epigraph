package com.mkrasikoff.epigraph.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "quotes")
@Data
public class Quote {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private String author;

    private String source;

    private boolean fav;

    /**
     * Хранится как "tag1,tag2" (строка)
     */
    private String tags;

    /**
     * Unix timestamp в миллисекундах
     */
    private Long added;
}
