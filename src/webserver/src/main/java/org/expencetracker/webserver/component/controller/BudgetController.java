package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Budget;
import org.expencetracker.webserver.component.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/budget")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addBudget(@Valid @RequestBody Budget budget) {
        return budgetService.addBudget(budget);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getBudgetsByUserId() {
        return budgetService.getBudgetsByUserId();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable String id, @Valid @RequestBody Budget budgetDetails) {
        return budgetService.updateBudget(id, budgetDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable String id) {
        return budgetService.deleteBudget(id);
    }

    @DeleteMapping("/user")
    public ResponseEntity<?> deleteBudgetsByUserId() {
        return budgetService.deleteBudgetsByUserId();
    }
}
