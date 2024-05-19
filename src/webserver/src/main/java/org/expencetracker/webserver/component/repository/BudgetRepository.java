package org.expencetracker.webserver.component.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.expencetracker.webserver.component.models.Budget;
import java.util.List;

public interface BudgetRepository extends MongoRepository<Budget, String> {
    List<Budget> findByUserId(String userId);
    List<Budget> findByName(String name);
    List<Budget> findByBudgetGreaterThan(int budget);
    List<Budget> findByBudgetLessThan(int budget);
    List<Budget> findByBudgetBetween(int minBudget, int maxBudget);
}
