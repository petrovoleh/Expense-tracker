package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Record;
import org.expencetracker.webserver.component.repository.RecordRepository;
import org.expencetracker.webserver.component.security.jwt.JwtUtils;
import org.expencetracker.webserver.component.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transactions")
public class RecordController {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/add")
    public ResponseEntity<?> addTransaction(@Valid @RequestBody Record transaction, @RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Set the user ID for the transaction
        transaction.setUserId(userDetails.getId());

        // Save the transaction to the database
        Record savedTransaction = recordRepository.save(transaction);

        // Return a response with the saved transaction
        return ResponseEntity.ok(savedTransaction);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTransactionById(@PathVariable String id) {
        Optional<Record> transaction = recordRepository.findById(id);
        if (transaction.isPresent()) {
            return ResponseEntity.ok(transaction.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getTransactionsByUserId(@RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Record> transactions = recordRepository.findByUserId(userDetails.getId());
        return ResponseEntity.ok(transactions);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTransaction(@PathVariable String id, @Valid @RequestBody Record transactionDetails) {
        Optional<Record> transactionOptional = recordRepository.findById(id);
        if (transactionOptional.isPresent()) {
            Record transaction = transactionOptional.get();
            transaction.setCategory(transactionDetails.getCategory());
            transaction.setDescription(transactionDetails.getDescription());
            transaction.setValue(transactionDetails.getValue());
            transaction.setUserId(transactionDetails.getUserId());
            Record updatedTransaction = recordRepository.save(transaction);
            return ResponseEntity.ok(updatedTransaction);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTransaction(@PathVariable String id) {
        Optional<Record> transactionOptional = recordRepository.findById(id);
        if (transactionOptional.isPresent()) {
            recordRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
