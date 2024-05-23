package org.expencetracker.webserver.component.repository;

import org.expencetracker.webserver.component.models.Message;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByUserId(String userId);
    List<Message> findByUserIdAndDateBetween(String userId, LocalDateTime fromDate, LocalDateTime toDate);
}
