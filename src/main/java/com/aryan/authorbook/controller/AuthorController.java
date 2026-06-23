package com.aryan.authorbook.controller;

import com.aryan.authorbook.entity.Author;
import com.aryan.authorbook.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorService service;

    @GetMapping
    public List<Author> getAllAuthors() {
        log.info("GET /authors - Fetching all authors");
        List<Author> authors = service.getAllAuthors();
        log.info("GET /authors - Successfully retrieved {} authors", authors.size());
        return authors;
    }

    @GetMapping("/{id}")
    public Author getAuthorById(@PathVariable Long id) {
        log.info("GET /authors/{} - Fetching author by ID", id);
        Author author = service.getAuthorById(id);
        log.info("GET /authors/{} - Successfully retrieved author: {}", id, author.getName());
        return author;
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importAuthors(@RequestParam("file") MultipartFile file) {
        int imported = service.importAuthorsFromCsv(file);
        return "Imported " + imported + " authors from CSV";
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<ByteArrayResource> exportAuthors() {
        byte[] csvData = service.exportAuthorsToCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=authors.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvData.length)
                .body(new ByteArrayResource(csvData));
    }

    @PostMapping
    public Author saveAuthor(@Valid @RequestBody Author author) {
        return service.saveAuthor(author);
    }

    @PostMapping("/bulk")
    public List<Author> saveAuthors(@Valid @RequestBody List<Author> authors) {
        return service.saveAuthors(authors);
    }

    @PutMapping("/{id}")
    public Author updateAuthor(@PathVariable Long id, @Valid @RequestBody Author author) {
        return service.updateAuthor(id, author);
    }

    @PutMapping("/bulk")
    public List<Author> updateAuthors(@Valid @RequestBody List<Author> authors) {
        log.info("PUT /authors/bulk - Updating {} authors", authors.size());
        List<Author> updatedAuthors = service.updateAuthors(authors);
        log.info("PUT /authors/bulk - Successfully updated {} authors", updatedAuthors.size());
        return updatedAuthors;
    }

    @DeleteMapping("/{id}")
    public void deleteAuthor(@PathVariable Long id) {
        log.info("DELETE /authors/{} - Deleting author", id);
        service.deleteAuthor(id);
        log.info("DELETE /authors/{} - Successfully deleted author", id);
    }
}