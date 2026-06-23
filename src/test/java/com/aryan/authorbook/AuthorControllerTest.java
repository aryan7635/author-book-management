package com.aryan.authorbook.controller;

import com.aryan.authorbook.service.AuthorService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthorControllerTest {

    private final AuthorService service =
            Mockito.mock(AuthorService.class);

    private final AuthorController controller =
            new AuthorController(service);

    @Test
    void controllerCreatedTest() {
        assertNotNull(controller);
    }
}