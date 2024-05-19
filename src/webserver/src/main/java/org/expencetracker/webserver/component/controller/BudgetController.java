package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Budget;
import org.expencetracker.webserver.component.repository.BudgetRepository;
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
@RequestMapping("/api/budget")
public class BudgetController {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/add")
    public ResponseEntity<?> addBudget(@Valid @RequestBody Budget budget, @RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Set the user ID for the budget
        budget.setUserId(userDetails.getId());

        // Save the budget to the database
        Budget savedBudget = budgetRepository.save(budget);

        // Return a response with the saved budget
        return ResponseEntity.ok(savedBudget);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getBudgetById(@PathVariable String id) {
        Optional<Budget> budget = budgetRepository.findById(id);
        if (budget.isPresent()) {
            return ResponseEntity.ok(budget.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getBudgetsByUserId(@RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Budget> budgets = budgetRepository.findByUserId(userDetails.getId());
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable String id, @Valid @RequestBody Budget budgetDetails) {
        Optional<Budget> budgetOptional = budgetRepository.findById(id);
        if (budgetOptional.isPresent()) {
            Budget budget = budgetOptional.get();
            budget.setName(budgetDetails.getName());
            budget.setBudget(budgetDetails.getBudget());
            budget.setUserId(budgetDetails.getUserId());
            Budget updatedBudget = budgetRepository.save(budget);
            return ResponseEntity.ok(updatedBudget);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable String id) {
        Optional<Budget> budgetOptional = budgetRepository.findById(id);
        if (budgetOptional.isPresent()) {
            budgetRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
