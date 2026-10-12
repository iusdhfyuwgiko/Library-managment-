package com.library.service;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class LibraryService {
    private static final int MAX_BOOKS_PER_MEMBER = 3;
    private static final Map<String, Book> LEGACY_BOOKS = new ConcurrentHashMap<>();
    private static final Map<String, Member> LEGACY_MEMBERS = new ConcurrentHashMap<>();
    private static final Map<String, Loan> LEGACY_LOANS = new ConcurrentHashMap<>();

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;

    public LibraryService() {
        this(null, null, null);
    }

    @Autowired
    public LibraryService(BookRepository bookRepository, MemberRepository memberRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
    }

    public void addBook(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book must not be null");
        }
        saveBook(book);
    }

    public void addMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("Member must not be null");
        }
        if (memberRepository != null) {
            memberRepository.save(member);
            return;
        }
        LEGACY_MEMBERS.put(member.getMemberId(), member);
    }

    public boolean issueBook(String bookId, String memberId) {
        if (bookId == null || memberId == null) {
            return false;
        }

        Book book = getBook(bookId);
        Member member = getMember(memberId);
        if (book == null || member == null) {
            return false;
        }

        if (hasActiveLoanForBook(bookId)) {
            return false;
        }

        long activeCount = countActiveLoansForMember(memberId);
        if (activeCount >= MAX_BOOKS_PER_MEMBER) {
            return false;
        }

        if (book.getAvailableCopies() <= 0) {
            return false;
        }

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        saveBook(book);

        Loan loan = new Loan(UUID.randomUUID().toString(), book, member, LocalDate.now(), LocalDate.now().plusDays(7));
        saveLoan(loan);
        return true;
    }

    public boolean returnBook(String bookId, LocalDate returnDate) {
        if (bookId == null) {
            return false;
        }

        Loan loan = findActiveLoanForBook(bookId);
        if (loan == null) {
            return false;
        }

        LocalDate actualReturnDate = returnDate == null ? LocalDate.now() : returnDate;
        loan.markReturned(actualReturnDate);

        Book book = loan.getBook();
        if (book != null) {
            book.setAvailableCopies(book.getAvailableCopies() + 1);
            saveBook(book);
        }
        saveLoan(loan);
        return true;
    }

    public double getCurrentFine(String bookId) {
        if (bookId == null) {
            return 0.0;
        }

        if (loanRepository != null) {
            Loan latestLoan = loanRepository
                    .findTopByBook_BookIdAndReturnDateIsNotNullOrderByReturnDateDesc(bookId)
                    .orElse(null);
            return latestLoan == null ? 0.0 : latestLoan.getFineAmount();
        }

        Loan latest = null;
        LocalDate latestDate = null;
        for (Loan loan : LEGACY_LOANS.values()) {
            LocalDate returned = loan.getReturnDate();
            if (returned == null || !bookId.equals(loan.getBookId())) {
                continue;
            }
            if (latestDate == null || returned.isAfter(latestDate)) {
                latest = loan;
                latestDate = returned;
            }
        }
        return latest == null ? 0.0 : latest.getFineAmount();
    }

    public List<Book> getAllBooks() {
        if (bookRepository != null) {
            return new ArrayList<>(bookRepository.findAll());
        }
        return new ArrayList<>(LEGACY_BOOKS.values());
    }

    public List<Member> getAllMembers() {
        if (memberRepository != null) {
            return new ArrayList<>(memberRepository.findAll());
        }
        return new ArrayList<>(LEGACY_MEMBERS.values());
    }

    public List<Loan> getAllLoans() {
        if (loanRepository != null) {
            return new ArrayList<>(loanRepository.findAll());
        }
        return new ArrayList<>(LEGACY_LOANS.values());
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.isBlank()) {
            return getAllBooks();
        }

        if (bookRepository != null) {
            List<Book> results = new ArrayList<>();
            results.addAll(bookRepository.findByTitleContainingIgnoreCase(query));
            results.addAll(bookRepository.findByAuthorContainingIgnoreCase(query));
            results.addAll(bookRepository.findByCategoryContainingIgnoreCase(query));
            results.addAll(bookRepository.findByBookIdContainingIgnoreCase(query));
            return results.stream().distinct().toList();
        }

        String lowerQuery = query.toLowerCase();
        return LEGACY_BOOKS.values().stream()
                .filter(book -> (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerQuery))
                        || (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lowerQuery))
                        || (book.getCategory() != null && book.getCategory().toLowerCase().contains(lowerQuery))
                        || (book.getBookId() != null && book.getBookId().toLowerCase().contains(lowerQuery)))
                .toList();
    }

    private Book getBook(String bookId) {
        if (bookId == null) {
            return null;
        }
        if (bookRepository != null) {
            return bookRepository.findById(bookId).orElse(null);
        }
        return LEGACY_BOOKS.get(bookId);
    }

    private Member getMember(String memberId) {
        if (memberId == null) {
            return null;
        }
        if (memberRepository != null) {
            return memberRepository.findById(memberId).orElse(null);
        }
        return LEGACY_MEMBERS.get(memberId);
    }

    private boolean hasActiveLoanForBook(String bookId) {
        if (bookId == null) {
            return false;
        }
        if (loanRepository != null) {
            return loanRepository.existsByBook_BookIdAndReturnDateIsNull(bookId);
        }
        return LEGACY_LOANS.values().stream()
                .anyMatch(loan -> bookId.equals(loan.getBookId()) && loan.getReturnDate() == null);
    }

    private long countActiveLoansForMember(String memberId) {
        if (memberId == null) {
            return 0L;
        }
        if (loanRepository != null) {
            return loanRepository.countByMember_MemberIdAndReturnDateIsNull(memberId);
        }
        return LEGACY_LOANS.values().stream()
                .filter(loan -> memberId.equals(loan.getMemberId()) && loan.getReturnDate() == null)
                .count();
    }

    private Loan findActiveLoanForBook(String bookId) {
        if (bookId == null) {
            return null;
        }
        if (loanRepository != null) {
            return loanRepository.findByBook_BookIdAndReturnDateIsNull(bookId).orElse(null);
        }
        return LEGACY_LOANS.values().stream()
                .filter(loan -> bookId.equals(loan.getBookId()) && loan.getReturnDate() == null)
                .findFirst()
                .orElse(null);
    }

    private void saveBook(Book book) {
        if (book == null) {
            return;
        }
        if (bookRepository != null) {
            bookRepository.save(book);
            return;
        }
        LEGACY_BOOKS.put(book.getBookId(), book);
    }

    private void saveLoan(Loan loan) {
        if (loan == null) {
            return;
        }
        if (loanRepository != null) {
            loanRepository.save(loan);
            return;
        }
        LEGACY_LOANS.put(loan.getLoanId(), loan);
    }
}