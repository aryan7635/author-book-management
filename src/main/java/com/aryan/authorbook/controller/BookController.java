package com.aryan.authorbook.controller;

import com.aryan.authorbook.entity.Book;
import com.aryan.authorbook.service.BookService;
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
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService service;

    @GetMapping
    public List<Book> getAllBooks() {
        log.info("GET /books - Fetching all books");
        List<Book> books = service.getAllBooks();
        log.info("GET /books - Successfully retrieved {} books", books.size());
        return books;
    }

    @PostMapping
    public Book saveBook(@RequestBody Book book) {
        return service.saveBook(book);
    }

    @PutMapping("/{id}")
    public Book updateBook(@PathVariable Long id, @RequestBody Book book) {
        return service.updateBook(id, book);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable Long id) {
        service.deleteBook(id);
    }

    @PostMapping("/bulk")
    public List<Book> bulkSave(@RequestBody List<Book> books) {
        return service.saveBooks(books);
    }

    @PutMapping("/bulk")
    public List<Book> bulkUpdate(@RequestBody List<Book> books) {
        return service.updateBooks(books);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importBooks(@RequestParam("file") MultipartFile file) {
        log.info("POST /books/import - Importing books from CSV file: {}", file.getOriginalFilename());
        int imported = service.importBooksFromCsv(file);
        log.info("POST /books/import - Successfully imported {} books", imported);
        return "Imported " + imported + " books from CSV";
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public ResponseEntity<ByteArrayResource> exportBooks() {
        log.info("GET /books/export - Exporting books to CSV");
        byte[] csvData = service.exportBooksToCsv();
        log.info("GET /books/export - Successfully exported {} bytes of CSV data", csvData.length);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=books.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(csvData.length)
                .body(new ByteArrayResource(csvData));
    }
}