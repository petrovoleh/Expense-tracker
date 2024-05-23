package org.expencetracker.webserver.component.service;

import org.expencetracker.webserver.component.models.Message;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.MessageRepository;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    public Message saveMessage(Message message) {
        User user = getAuthenticatedUser();
        if (user != null) {
            message.setUserId(user.getId());
            return messageRepository.save(message);
        }
        return null;
    }

    public List<Message> getMessagesByUserIdAndDateRange(String userId, LocalDateTime from, LocalDateTime to) {
        return messageRepository.findByUserIdAndDateBetween(userId, from, to);
    }

    public List<Message> getAllMessagesByUserId(String userId) {
        return messageRepository.findByUserId(userId);
    }
}
