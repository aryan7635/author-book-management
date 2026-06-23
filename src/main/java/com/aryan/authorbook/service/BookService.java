package com.aryan.authorbook.service;

import com.aryan.authorbook.entity.Author;
import com.aryan.authorbook.entity.Book;
import com.aryan.authorbook.repository.AuthorRepository;
import com.aryan.authorbook.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository repository;
    private final AuthorRepository authorRepository;

    public List<Book> getAllBooks() {
        return repository.findAll();
    }

    public Book saveBook(Book book) {

        Long authorId = book.getAuthor().getId();

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Author not found with id: " + authorId
                        ));

        book.setAuthor(author);
        return repository.save(book);
    }

    public Book updateBook(
            Long id,
            Book book) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(
                    "Book not found with id: " + id
            );
        }
        book.setId(id);

        return repository.save(book);
    }

    public void deleteBook(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(
                    "Book not found with id: " + id
            );
        }
        repository.deleteById(id);
    }

    public List<Book> saveBooks(
            List<Book> books) {

        return repository.saveAll(books);
    }

    public List<Book> updateBooks(List<Book> books) {
        return repository.saveAll(books);
    }

    public int importBooksFromCsv(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("CSV file is empty");
        }

        List<Book> toSave = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null || headerLine.isBlank()) {
                throw new RuntimeException("CSV header is missing");
            }

            Map<String, Integer> headers = indexHeaders(splitCsvLine(headerLine));
            if (!headers.containsKey("title") || !headers.containsKey("price") || !headers.containsKey("authorid")) {
                throw new RuntimeException("CSV must contain title, price and authorId columns");
            }

            String line;
            int rowNumber = 1; // Header row number
            while ((line = reader.readLine()) != null) {
                rowNumber++;
                if (line.isBlank()) {
                    continue;
                }

                List<String> row = splitCsvLine(line);
                String title = getColumnValue(row, headers, "title");
                String priceValue = getColumnValue(row, headers, "price");
                String authorIdValue = getColumnValue(row, headers, "authorid");

                if (title == null || title.isBlank()) {
                    continue;
                }
                if (authorIdValue == null || authorIdValue.isBlank()) {
                    throw new RuntimeException("authorId is missing at CSV row " + rowNumber);
                }
                if (priceValue == null || priceValue.isBlank()) {
                    throw new RuntimeException("price is missing at CSV row " + rowNumber);
                }

                long authorId;
                double price;
                try {
                    authorId = Long.parseLong(authorIdValue.trim());
                    price = Double.parseDouble(priceValue.trim());
                } catch (NumberFormatException ex) {
                    throw new RuntimeException("Invalid numeric value at CSV row " + rowNumber, ex);
                }

                Author author = authorRepository.findById(authorId)
                        .orElseThrow(() -> new RuntimeException("Author not found with id: " + authorId));

                Book book = new Book();
                String idValue = getColumnValue(row, headers, "id");
                if (idValue != null && !idValue.isBlank()) {
                    try {
                        book.setId(Long.parseLong(idValue.trim()));
                    } catch (NumberFormatException ex) {
                        throw new RuntimeException("Invalid id at CSV row " + rowNumber, ex);
                    }
                }
                book.setTitle(title.trim());
                book.setPrice(price);
                book.setAuthor(author);
                toSave.add(book);
            }

            repository.saveAll(toSave);
            return toSave.size();
        } catch (IOException e) {
            throw new RuntimeException("Failed to import books CSV", e);
        }
    }

    public byte[] exportBooksToCsv() {
        log.info("Starting CSV export for books");
        StringBuilder csv = new StringBuilder("id,title,price,authorId\n");
        List<Book> books = repository.findAll();
        for (Book book : books) {
            Long authorId = book.getAuthor() == null ? null : book.getAuthor().getId();
            csv.append(book.getId() == null ? "" : book.getId())
                    .append(",")
                    .append(escapeCsv(book.getTitle()))
                    .append(",")
                    .append(book.getPrice() == null ? "" : book.getPrice())
                    .append(",")
                    .append(authorId == null ? "" : authorId)
                    .append("\n");
        }
        byte[] result = csv.toString().getBytes(StandardCharsets.UTF_8);
        log.info("Successfully exported {} books to CSV ({} bytes)", books.size(), result.length);
        return result;
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
}
