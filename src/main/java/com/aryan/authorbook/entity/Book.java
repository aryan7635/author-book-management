package com.aryan.authorbook.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Auto-generated ID, leave blank on create")
    private Long id;

    @Schema(example = "Clean Code", description = "Title of the book")
    private String title;

    @Schema(example = "499.99", description = "Price of the book")
    private Double price;

    @ManyToOne
    @Schema(description = "Only provide the author id, e.g. {\"id\": 1}")
    private Author author;
}
