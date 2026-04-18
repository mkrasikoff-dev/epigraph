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

    @Column(name = "user_id")
    private Long userId;

    @NotBlank(message = "Quote text must not be empty")
    @Size(max = 3000, message = "Размер цитаты не должен превышать 3000 символов")
    @Column(length = 3000)
    private String text;

    @Size(max = 255, message = "Author name must not exceed 255 characters")
    @Column(length = 255)
    private String author;

    @Size(max = 500, message = "Source must not exceed 500 characters")
    @Column(length = 500)
    private String source;

    private boolean fav;

    /**
     * Хранится как "tag1,tag2" (строка)
     */
    @Size(max = 500, message = "Tags must not exceed 500 characters")
    @Column(length = 500)
    private String tags;

    /**
     * Unix timestamp в миллисекундах
     */
    @Column(updatable = false)
    private Long added;
}
