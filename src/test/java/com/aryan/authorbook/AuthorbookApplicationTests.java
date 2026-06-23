package com.aryan.authorbook;

import com.aryan.authorbook.entity.Author;
import com.aryan.authorbook.repository.AuthorRepository;
import com.aryan.authorbook.service.AuthorService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuthorServiceTest {

	@Autowired
	private AuthorService service;

	@Autowired
	private AuthorRepository authorRepository;

	@AfterEach
	void cleanUp() {
		// Remove test author so the test can run repeatedly
		authorRepository.findByEmail("author123@gmail.com").ifPresent(authorRepository::delete);
	}

	@Test
	void saveAuthorTest() {

		// Clean up before test in case a previous run left dirty data
		authorRepository.findByEmail("author123@gmail.com").ifPresent(authorRepository::delete);

		Author author = new Author(
						null,
						"Author123",
						"author123@gmail.com"
				);

		Author saved = service.saveAuthor(author);

		assertNotNull(saved);
		assertNotNull(saved.getId());
		assertEquals("Author123", saved.getName());
	}

	@Test
	void duplicateEmailShouldFail() {
		authorRepository.findByEmail("author123@gmail.com").ifPresent(authorRepository::delete);

		Author first = new Author(null, "AuthorA", "author123@gmail.com");
		Author second = new Author(null, "AuthorB", "author123@gmail.com");

		service.saveAuthor(first);

		RuntimeException exception = assertThrows(RuntimeException.class, () -> service.saveAuthor(second));
		assertEquals("Author with email already exists", exception.getMessage());
	}
}