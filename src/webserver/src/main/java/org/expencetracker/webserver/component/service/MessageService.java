package org.expencetracker.webserver.component.service;

import org.expencetracker.webserver.component.models.Message;
import org.expencetracker.webserver.component.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByUserIdAndDateRange(String userId, LocalDateTime fromDate, LocalDateTime toDate) {
        return messageRepository.findByUserIdAndDateBetween(userId, fromDate, toDate);
    }

    public List<Message> getAllMessagesByUserId(String userId) {
        return messageRepository.findByUserId(userId);
    }
}
