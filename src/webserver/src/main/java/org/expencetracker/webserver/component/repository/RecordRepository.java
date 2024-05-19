package org.expencetracker.webserver.component.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.expencetracker.webserver.component.models.Record;
import java.util.List;

public interface RecordRepository extends MongoRepository<Record, String> {
    List<Record> findByUserId(String userId);
    List<Record> findByCategory(String category);
    List<Record> findByPlace(String place);
    List<Record> findByValueGreaterThan(double value);
    List<Record> findByValueLessThan(double value);
    List<Record> findByValueBetween(double minValue, double maxValue);
}
