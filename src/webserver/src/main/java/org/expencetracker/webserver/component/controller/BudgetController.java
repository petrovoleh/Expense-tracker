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

    /**
     * Adds a new budget.
     *
     * @param budget the budget to be added
     * @return a ResponseEntity containing the saved budget
     */
    @PostMapping("/add")
    public ResponseEntity<?> addBudget(@RequestBody Budget budget) {
        return budgetService.addBudget(budget);
    }

    /**
     * Gets all budgets for the authenticated user.
     *
     * @return a ResponseEntity containing the list of budgets
     */
    @GetMapping("/user")
    public ResponseEntity<?> getBudgetsByUserId() {
        return budgetService.getBudgetsByUserId();
    }

    /**
     * Updates an existing budget.
     *
     * @param id the ID of the budget to be updated
     * @param budgetDetails the new details of the budget
     * @return a ResponseEntity containing the updated budget
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateBudget(@PathVariable String id, @RequestBody Budget budgetDetails) {
        return budgetService.updateBudget(id, budgetDetails);
    }

    /**
     * Deletes a budget by its ID.
     *
     * @param id the ID of the budget to be deleted
     * @return a ResponseEntity indicating the result of the operation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBudget(@PathVariable String id) {
        return budgetService.deleteBudget(id);
    }

    /**
     * Deletes all budgets for the authenticated user.
     *
     * @return a ResponseEntity indicating the result of the operation
     */
    @DeleteMapping("/user")
    public ResponseEntity<?> deleteBudgetsByUserId() {
        return budgetService.deleteBudgetsByUserId();
    }
}
