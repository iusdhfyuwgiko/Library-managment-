package com.library.repository;

import com.library.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {
    boolean existsByBook_BookIdAndReturnDateIsNull(String bookId);

    long countByMember_MemberIdAndReturnDateIsNull(String memberId);

    Optional<Loan> findByBook_BookIdAndReturnDateIsNull(String bookId);

    Optional<Loan> findTopByBook_BookIdAndReturnDateIsNotNullOrderByReturnDateDesc(String bookId);
}
