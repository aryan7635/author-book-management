package com.aryan.authorbook.service;

import com.aryan.authorbook.entity.Book;
import com.aryan.authorbook.repository.BookRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository repository;

    @InjectMocks
    private BookService service;

    @Test
    void getAllBooksTest() {

        Book book = new Book();
        book.setTitle("Spring Boot");

        when(repository.findAll()).thenReturn(List.of(book));

        assertEquals(1, service.getAllBooks().size());
    }
}