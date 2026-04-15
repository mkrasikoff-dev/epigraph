package com.mkrasikoff.epigraph.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Entity
@Table(name = "quotes")
@Data
public class Quote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Quote text must not be empty")
    @Size(max = 3000, message = "Quote text must not exceed 3000 characters")
    @Column(length = 3000)
    private String text;

    @Size(max = 255, message = "Author name must not exceed 255 characters")
    @Column(length = 255)
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
    @Column(updatable = false)
    private Long added;
}
