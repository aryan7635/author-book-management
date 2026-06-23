package com.aryan.authorbook.controller;

import com.aryan.authorbook.service.BookService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class BookControllerTest {

    private final BookService service =
            Mockito.mock(BookService.class);

    private final BookController controller =
            new BookController(service);

    @Test
    void controllerCreatedTest() {
        assertNotNull(controller);
    }
}