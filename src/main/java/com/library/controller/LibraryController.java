package com.library.controller;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.LibraryService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class LibraryController {
    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/books")
    public List<Book> getBooks() {
        return libraryService.getAllBooks();
    }

    @GetMapping("/books/search")
    public List<Book> searchBooks(@RequestParam(required = false) String query) {
        return libraryService.searchBooks(query);
    }

    @PostMapping("/books")
    public Book addBook(@RequestBody Book book) {
        libraryService.addBook(book);
        return book;
    }

    @GetMapping("/members")
    public List<Member> getMembers() {
        return libraryService.getAllMembers();
    }

    @PostMapping("/members")
    public Member addMember(@RequestBody Member member) {
        libraryService.addMember(member);
        return member;
    }

    @PostMapping("/books/{bookId}/issue")
    public Map<String, Boolean> issueBook(@PathVariable String bookId, @RequestParam String memberId) {
        return Map.of("success", libraryService.issueBook(bookId, memberId));
    }

    @PostMapping("/books/{bookId}/return")
    public Map<String, Boolean> returnBook(@PathVariable String bookId, @RequestParam(required = false) String returnDate) {
        LocalDate date = returnDate == null || returnDate.isBlank() ? LocalDate.now() : LocalDate.parse(returnDate);
        return Map.of("success", libraryService.returnBook(bookId, date));
    }

    @GetMapping("/books/{bookId}/fine")
    public Map<String, Double> getFine(@PathVariable String bookId) {
        return Map.of("fineAmount", libraryService.getCurrentFine(bookId));
    }
}
