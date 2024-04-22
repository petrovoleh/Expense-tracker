package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Transaction;
import org.expencetracker.webserver.component.repository.TransactionRepository;
import org.expencetracker.webserver.component.security.jwt.JwtUtils;
import org.expencetracker.webserver.component.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/add")
    public ResponseEntity<?> addTransaction(@Valid @RequestBody Transaction transaction, @RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Set the user ID for the transaction
        transaction.setUserId(userDetails.getId());

        // Save the transaction to the database
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Return a response with the saved transaction
        return ResponseEntity.ok(savedTransaction);
    }
}
