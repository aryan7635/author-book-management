package com.aryan.authorbook.repository;

import com.aryan.authorbook.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository
        extends JpaRepository<Book, Long> {
}