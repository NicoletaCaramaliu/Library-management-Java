package com.example.library.controller;

import com.example.library.model.Loan;
import com.example.library.service.LoanService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    // GET /api/loans
    @GetMapping
    public List<Loan> getAll() {
        return loanService.getAllLoans();
    }

    // GET /api/loans/{id}
    @GetMapping("/{id}")
    public Loan getById(@PathVariable Long id) {
        return loanService.getLoanById(id);
    }

    // GET /api/loans/user/{userId}
    @GetMapping("/user/{userId}")
    public List<Loan> getForUser(@PathVariable Long userId) {
        return loanService.getLoansForUser(userId);
    }

    // GET /api/loans/overdue
    @GetMapping("/overdue")
    public List<Loan> getOverdue() {
        return loanService.getOverdueLoans();
    }

    // POST /api/loans?userId=1&bookId=2
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Loan create(@RequestParam Long userId,
                       @RequestParam Long bookId) {
        return loanService.createLoan(userId, bookId);
    }

    // POST /api/loans/{id}/return
    @PostMapping("/{id}/return")
    public Loan returnLoan(@PathVariable Long id) {
        return loanService.returnLoan(id);
    }

    // DELETE /api/loans/{id}
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        loanService.deleteLoan(id);
    }
}
