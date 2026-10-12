package com.library.service;

import com.library.model.Book;
import com.library.model.Member;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServicePersistenceTest {

    @Test
    void shouldPersistBookAndMemberDataAcrossServiceInstances() {
        LibraryService first = new LibraryService();
        first.addBook(new Book("B-900", "Database Design", "John Doe", "Technology", 2));
        first.addMember(new Member("M-900", "Dana", "dana@example.com"));

        LibraryService second = new LibraryService();

        assertFalse(second.getAllBooks().isEmpty());
        assertFalse(second.getAllMembers().isEmpty());
        assertEquals("B-900", second.getAllBooks().get(0).getBookId());
        assertEquals("M-900", second.getAllMembers().get(0).getMemberId());
    }
}
