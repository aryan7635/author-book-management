package com.aryan.authorbook;

import com.aryan.authorbook.controller.AuthorController;
import com.aryan.authorbook.entity.Author;
import com.aryan.authorbook.security.SecurityConfig;
import com.aryan.authorbook.service.AuthorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@WebMvcTest(controllers = AuthorController.class)
@Import(SecurityConfig.class)
class SecurityConfigWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthorService authorService;

    @BeforeEach
    void setUp() {
        when(authorService.getAllAuthors()).thenReturn(List.of());
        when(authorService.getAuthorById(1L)).thenReturn(new Author(1L, "Author1", "author1@example.com"));
        when(authorService.saveAuthor(any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(0);
            return new Author(1L, author.getName(), author.getEmail());
        });
        when(authorService.updateAuthor(eq(1L), any(Author.class))).thenAnswer(invocation -> {
            Author author = invocation.getArgument(1);
            return new Author(1L, author.getName(), author.getEmail());
        });
        doNothing().when(authorService).deleteAuthor(1L);
    }

    @Test
    void noLoginGetAuthorsShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/authors"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void librarianGetAuthorsShouldReturnOk() throws Exception {
        mockMvc.perform(get("/authors").with(httpBasic("librarian", "lib123")))
                .andExpect(status().isOk());
    }

    @Test
    void librarianPostAuthorsShouldReturnForbidden() throws Exception {
        Author author = new Author(null, "AuthorA", "authora@example.com");

        mockMvc.perform(post("/authors")
                        .with(httpBasic("librarian", "lib123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isForbidden());
    }

    @Test
    void librarianDeleteAuthorShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/authors/1").with(httpBasic("librarian", "lib123")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminGetAuthorsShouldReturnOk() throws Exception {
        mockMvc.perform(get("/authors").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }

    @Test
    void adminPostAuthorsShouldReturnSuccess() throws Exception {
        Author author = new Author(null, "AuthorA", "authora@example.com");

        mockMvc.perform(post("/authors")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isOk());
    }

    @Test
    void adminPutAuthorShouldReturnSuccess() throws Exception {
        Author author = new Author(null, "AuthorA", "authora@example.com");

        mockMvc.perform(put("/authors/1")
                        .with(httpBasic("admin", "admin123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(author)))
                .andExpect(status().isOk());
    }

    @Test
    void adminDeleteAuthorShouldReturnSuccess() throws Exception {
        mockMvc.perform(delete("/authors/1").with(httpBasic("admin", "admin123")))
                .andExpect(status().isOk());
    }
}

