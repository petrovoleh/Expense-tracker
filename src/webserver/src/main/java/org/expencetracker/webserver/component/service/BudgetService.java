package org.expencetracker.webserver.component.service;

import org.expencetracker.webserver.component.models.Budget;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.BudgetRepository;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    @Autowired
    private BudgetRepository budgetRepository;

    @Autowired
    private UserRepository userRepository;

    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    public ResponseEntity<?> addBudget(Budget budget) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        budget.setUserId(user.getId());
        Budget savedBudget = budgetRepository.save(budget);
        return ResponseEntity.ok(savedBudget);
    }

    public ResponseEntity<?> getBudgetsByUserId() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        List<Budget> budgets = budgetRepository.findByUserId(user.getId());
        return ResponseEntity.ok(budgets);
    }

    public ResponseEntity<?> updateBudget(String id, Budget budgetDetails) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Optional<Budget> budgetOptional = budgetRepository.findById(id);
        if (budgetOptional.isPresent()) {
            Budget budget = budgetOptional.get();
            if (!budget.getUserId().equals(user.getId())) {
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

    public ResponseEntity<?> deleteBudget(String id) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Optional<Budget> budgetOptional = budgetRepository.findById(id);
        if (budgetOptional.isPresent()) {
            Budget budget = budgetOptional.get();
            if (!budget.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You are not authorized to delete this budget");
            }

            budgetRepository.deleteById(id);
            return ResponseEntity.ok("Budget deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    public ResponseEntity<?> deleteBudgetsByUserId() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        List<Budget> userBudgets = budgetRepository.findByUserId(user.getId());
        if (userBudgets.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        budgetRepository.deleteAll(userBudgets);
        return ResponseEntity.ok("Budgets deleted successfully");
    }
}
