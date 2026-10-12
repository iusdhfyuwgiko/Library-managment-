package com.library.service;

import com.library.model.Book;
import com.library.model.Member;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LibraryServiceTest {

    @Autowired
    private LibraryService service;

    @Test
    void shouldIssueAndReturnBookSuccessfully() {
        Book book = new Book("B-101", "Clean Code", "Robert Martin", "Programming", 2);
        Member member = new Member("M-1001", "Alice", "alice@example.com");

        service.addBook(book);
        service.addMember(member);

        assertTrue(service.issueBook("B-101", "M-1001"));
        assertEquals(1, service.getAllBooks().stream().filter(b -> b.getBookId().equals("B-101")).findFirst().orElseThrow().getAvailableCopies());

        assertTrue(service.returnBook("B-101", LocalDate.now().plusDays(1)));
        assertEquals(2, service.getAllBooks().stream().filter(b -> b.getBookId().equals("B-101")).findFirst().orElseThrow().getAvailableCopies());
    }

    @Test
    void shouldStopIssueWhenBookNotAvailable() {
        Book book = new Book("B-202", "Effective Java", "Joshua Bloch", "Programming", 1);
        Member member = new Member("M-2002", "Bob", "bob@example.com");

        service.addBook(book);
        service.addMember(member);

        assertTrue(service.issueBook("B-202", "M-2002"));
        assertFalse(service.issueBook("B-202", "M-2002"));
    }

    @Test
    void shouldCalculateFineForLateReturn() {
        Book book = new Book("B-303", "Java The Complete Reference", "Herbert Schildt", "Programming", 1);
        Member member = new Member("M-3003", "Charlie", "charlie@example.com");

        service.addBook(book);
        service.addMember(member);

        service.issueBook("B-303", "M-3003");

        assertTrue(service.returnBook("B-303", LocalDate.now().plusDays(12)));
        assertEquals(50.0, service.getCurrentFine("B-303"), 0.01);
    }
}
