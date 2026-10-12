package com.library.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "loans")
public class Loan {
    @Id
    @Column(name = "loan_id")
    private String loanId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonBackReference("book-loans")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    @JsonBackReference("member-loans")
    private Member member;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "return_date")
    private LocalDate returnDate;

    @Column(name = "fine_amount")
    private double fineAmount;

    public Loan() {
    }

    public Loan(String loanId, Book book, Member member, LocalDate issueDate, LocalDate dueDate) {
        this.loanId = loanId;
        this.book = book;
        this.member = member;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    public void markReturned(LocalDate returnDate) {
        this.returnDate = returnDate;
        this.fineAmount = calculateFine(returnDate);
    }

    public double calculateFine(LocalDate date) {
        if (date == null || !date.isAfter(dueDate)) {
            return 0.0;
        }
        return 50.0;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public String getBookId() {
        return book != null ? book.getBookId() : null;
    }

    public String getMemberId() {
        return member != null ? member.getMemberId() : null;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "loanId='" + loanId + '\'' +
                ", bookId='" + (book != null ? book.getBookId() : null) + '\'' +
                ", memberId='" + (member != null ? member.getMemberId() : null) + '\'' +
                ", issueDate=" + issueDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", fineAmount=" + fineAmount +
                '}';
    }
}
