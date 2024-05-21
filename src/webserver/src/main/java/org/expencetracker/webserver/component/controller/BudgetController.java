package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Budget;
import org.expencetracker.webserver.component.repository.BudgetRepository;
import org.expencetracker.webserver.component.security.jwt.JwtUtils;
import org.expencetracker.webserver.component.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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


    @GetMapping("/user")
    public ResponseEntity<?> getBudgetsByUserId(@RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<Budget> budgets = budgetRepository.findByUserId(userDetails.getId());
        return ResponseEntity.ok(budgets);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable String id, @Valid @RequestBody Budget budgetDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String currentUserId = userDetails.getId();

        Optional<Budget> budgetOptional = budgetRepository.findById(id);
        if (budgetOptional.isPresent()) {
            Budget budget = budgetOptional.get();

            // Check if the authenticated user is the owner of the budget
            if (!budget.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to update this budget");
            }

            budget.setName(budgetDetails.getName());
            budget.setBudget(budgetDetails.getBudget());
            Budget updatedBudget = budgetRepository.save(budget);
            return ResponseEntity.ok(updatedBudget);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@RequestHeader("Authorization") String token, @PathVariable String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String currentUserId = userDetails.getId();

        Optional<Budget> budgetOptional = budgetRepository.findById(id);
        if (budgetOptional.isPresent()) {
            Budget budget = budgetOptional.get();

            // Check if the authenticated user is the owner of the budget
            if (!budget.getUserId().equals(currentUserId)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this budget");
            }

            budgetRepository.deleteById(id);
            return ResponseEntity.ok("Budget deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteBudgetsByUserId(@RequestHeader("Authorization") String token) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String currentUserId = userDetails.getId();

        // Find budgets associated with the authenticated user
        List<Budget> userBudgets = budgetRepository.findByUserId(currentUserId);

        // Check if the user has any budgets
        if (userBudgets.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Delete all budgets associated with the authenticated user
        budgetRepository.deleteAll(userBudgets);

        return ResponseEntity.ok("Budgets deleted successfully");
    }

}
