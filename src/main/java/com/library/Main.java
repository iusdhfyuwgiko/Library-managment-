package com.library;

import com.library.model.Book;
import com.library.model.Member;
import com.library.service.LibraryService;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        LibraryService service = new LibraryService();
        seedData(service);
        showMenu(service);
    }

    private static void seedData(LibraryService service) {
        service.addBook(new Book("B-101", "Clean Code", "Robert Martin", "Programming", 2));
        service.addBook(new Book("B-102", "Java: The Complete Reference", "Herbert Schildt", "Programming", 3));
        service.addBook(new Book("B-103", "The Alchemist", "Paulo Coelho", "Fiction", 2));
        service.addMember(new Member("M-1001", "Alice", "alice@example.com"));
        service.addMember(new Member("M-1002", "Bob", "bob@example.com"));
    }

    private static void showMenu(LibraryService service) {
        // try-with-resources closes the Scanner automatically when the method exits
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== ISME Knowledge Hub ===");
                System.out.println("1. View Books");
                System.out.println("2. Search Books");
                System.out.println("3. Add Book");
                System.out.println("4. Add Member");
                System.out.println("5. Issue Book");
                System.out.println("6. Return Book");
                System.out.println("7. Exit");
                System.out.print("Choose an option: ");

                String input = scanner.nextLine();

                try {
                    int option = Integer.parseInt(input);
                    switch (option) {
                        case 1 -> printBooks(service.getAllBooks());
                        case 2 -> {
                            System.out.print("Enter title keyword: ");
                            String keyword = scanner.nextLine();
                            printBooks(service.searchBooks(keyword));
                        }
                        case 3 -> {
                            System.out.print("Book ID: ");
                            String id = scanner.nextLine();
                            System.out.print("Title: ");
                            String title = scanner.nextLine();
                            System.out.print("Author: ");
                            String author = scanner.nextLine();
                            System.out.print("Category: ");
                            String category = scanner.nextLine();
                            System.out.print("Copies: ");
                            int copies = Integer.parseInt(scanner.nextLine());
                            service.addBook(new Book(id, title, author, category, copies));
                            System.out.println("Book added successfully.");
                        }
                        case 4 -> {
                            System.out.print("Member ID: ");
                            String id = scanner.nextLine();
                            System.out.print("Name: ");
                            String name = scanner.nextLine();
                            System.out.print("Email: ");
                            String email = scanner.nextLine();
                            service.addMember(new Member(id, name, email));
                            System.out.println("Member added successfully.");
                        }
                        case 5 -> {
                            System.out.print("Book ID: ");
                            String bookId = scanner.nextLine();
                            System.out.print("Member ID: ");
                            String memberId = scanner.nextLine();
                            System.out.println(service.issueBook(bookId, memberId)
                                    ? "Book issued successfully."
                                    : "Issue failed. Check availability or member limit.");
                        }
                        case 6 -> {
                            System.out.print("Book ID: ");
                            String bookId = scanner.nextLine();
                            double fine = service.getCurrentFine(bookId);
                            boolean returned = service.returnBook(bookId, LocalDate.now());
                            if (returned) {
                                System.out.println("Book returned successfully.");
                                if (fine > 0) {
                                    System.out.println("Fine charged: $" + fine);
                                }
                            } else {
                                System.out.println("Return failed. No active issue found for that book.");
                            }
                        }
                        case 7 -> {
                            System.out.println("Goodbye!");
                            return;
                        }
                        default -> System.out.println("Invalid option. Please try again.");
                    }
                } catch (Exception e) {
                    System.out.println("Invalid input: " + e.getMessage());
                }
            }
        }
    }

    private static void printBooks(List<Book> books) {
        if (books.isEmpty()) {
            System.out.println("No books found.");
            return;
        }

        System.out.println("Books:");
        for (Book book : books) {
            System.out.printf("- %s | %s | %s | Available: %d/%d%n",
                    book.getBookId(),
                    book.getTitle(),
                    book.getAuthor(),
                    book.getAvailableCopies(),
                    book.getTotalCopies());
        }
    }
}