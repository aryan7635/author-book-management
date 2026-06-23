package com.aryan.authorbook.service;

import com.aryan.authorbook.entity.Author;
import com.aryan.authorbook.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository repository;

    public List<Author> getAllAuthors() {
        return repository.findAll();
    }

    public Author getAuthorById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Author not found with id: " + id));
    }

    public Author saveAuthor(Author author) {
        String email = normalizeEmail(author.getEmail());
        validateDuplicateEmailForCreate(email);
        author.setEmail(email);
        log.info("Saving Author: {}", author.getName());
        return repository.save(author);
    }

    public int importAuthorsFromCsv(MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("CSV file is empty");
        }

        List<Author> toSave = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new RuntimeException("CSV header is missing");
            }

            Map<String, Integer> headers = indexHeaders(splitCsvLine(headerLine));
            if (!headers.containsKey("name")) {
                throw new RuntimeException("CSV must contain 'name' column");
            }
            if (!headers.containsKey("email")) {
                throw new RuntimeException("CSV must contain 'email' column");
            }

            Set<String> seenEmails = new HashSet<>();

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                List<String> row = splitCsvLine(line);
                String name = getColumnValue(row, headers, "name");
                if (name == null || name.isBlank()) {
                    continue;
                }

                String email = getColumnValue(row, headers, "email");
                String normalizedEmail = normalizeEmail(email);
                if (normalizedEmail == null) {
                    throw new RuntimeException("Author email must not be blank");
                }
                if (!seenEmails.add(normalizedEmail)) {
                    throw new RuntimeException("Duplicate email found in CSV: " + normalizedEmail);
                }
                Author author = repository.findByEmail(normalizedEmail).orElseGet(Author::new);
                author.setName(name.trim());
                author.setEmail(normalizedEmail);
                toSave.add(author);
            }

            repository.saveAll(toSave);
            return toSave.size();
        } catch (IOException e) {
            throw new RuntimeException("Failed to import authors CSV", e);
        }
    }

    public byte[] exportAuthorsToCsv() {
        StringBuilder csv = new StringBuilder("id,name,email\n");
        for (Author author : repository.findAll()) {
            csv.append(author.getId() == null ? "" : author.getId())
                    .append(",")
                    .append(escapeCsv(author.getName()))
                    .append(",")
                    .append(escapeCsv(author.getEmail()))
                    .append("\n");
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    private Map<String, Integer> indexHeaders(List<String> headerCells) {
        Map<String, Integer> headers = new HashMap<>();
        for (int i = 0; i < headerCells.size(); i++) {
            headers.put(headerCells.get(i).trim().toLowerCase(), i);
        }
        return headers;
    }

    private String getColumnValue(List<String> row, Map<String, Integer> headers, String headerName) {
        Integer index = headers.get(headerName.toLowerCase());
        if (index == null || index >= row.size()) {
            return null;
        }
        return row.get(index);
    }

    private List<String> splitCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            if (ch == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        values.add(current.toString());
        return values;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    public Author updateAuthor(Long id, Author author) {

        if(!repository.existsById(id)) {
            throw new RuntimeException(
                    "Author not found with id: " + id
            );
        }
        String email = normalizeEmail(author.getEmail());
        if (email == null) {
            throw new RuntimeException("Author email must not be blank");
        }
        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new RuntimeException("Author with email already exists");
        }
        author.setEmail(email);
        author.setId(id);
        return repository.save(author);
    }

    public void deleteAuthor(Long id) {
        if(!repository.existsById(id)) {
            throw new RuntimeException(
                    "Author not found with id: " + id
            );
        }
        log.info("Deleting Author Id: {}", id);
        repository.deleteById(id);
    }

    public List<Author> saveAuthors(List<Author> authors) {
        for (Author author : authors) {
            author.setEmail(normalizeEmail(author.getEmail()));
            validateDuplicateEmailForCreate(author.getEmail());
        }
        return repository.saveAll(authors);
    }

    public List<Author> updateAuthors(List<Author> authors) {
        log.info("Updating {} authors in bulk", authors.size());
        for (Author author : authors) {
            if (author.getId() == null) {
                log.error("Author id is required for bulk update");
                throw new RuntimeException("Author id is required for bulk update");
            }
            String email = normalizeEmail(author.getEmail());
            if (email == null) {
                log.error("Author email must not be blank");
                throw new RuntimeException("Author email must not be blank");
            }
            if (repository.existsByEmailAndIdNot(email, author.getId())) {
                log.error("Author with email {} already exists", email);
                throw new RuntimeException("Author with email already exists");
            }
            author.setEmail(email);
        }
        List<Author> updatedAuthors = repository.saveAll(authors);
        log.info("Successfully updated {} authors", updatedAuthors.size());
        return updatedAuthors;
    }

    private void validateDuplicateEmailForCreate(String email) {
        if (email == null) {
            throw new RuntimeException("Author email must not be blank");
        }
        if (repository.existsByEmail(email)) {
            throw new RuntimeException("Author with email already exists");
        }
    }

    private String normalizeEmail(String email) {
        if (email == null) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        return normalized.isBlank() ? null : normalized;
    }

}