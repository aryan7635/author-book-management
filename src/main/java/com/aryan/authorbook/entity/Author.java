package com.aryan.authorbook.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Auto-generated ID, leave blank on create")
    private Long id;

    @NotBlank(message = "Name must not be blank")
    @Schema(example = "Aryan Singh", description = "Author name")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    @Schema(example = "aryan.singh01@nagarro.com", description = "Unique valid email address")
    private String email;
}