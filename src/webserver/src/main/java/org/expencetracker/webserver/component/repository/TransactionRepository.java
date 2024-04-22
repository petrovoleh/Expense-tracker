package org.expencetracker.webserver.component.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.expencetracker.webserver.component.models.Transaction;
import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUserId(String userId);
    List<Transaction> findByCategory(String category);
    List<Transaction> findByPlace(String place);
    List<Transaction> findByValueGreaterThan(double value);
    List<Transaction> findByValueLessThan(double value);
    List<Transaction> findByValueBetween(double minValue, double maxValue);
}
