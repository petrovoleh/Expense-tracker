package org.expencetracker.webserver.component.service;

import org.expencetracker.webserver.component.models.Message;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.MessageRepository;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    public Message saveMessage(Message message) {
        User user = getAuthenticatedUser();
        message.setUserId(user.getId());
        return messageRepository.save(message);
    }

    public List<Message> getMessagesByDateRange(LocalDateTime from, LocalDateTime to) {
        User user = getAuthenticatedUser();
        return messageRepository.findByUserIdAndDateBetween(user.getId(), from, to);
    }

    public List<Message> getAllMessagesByAuthenticatedUser() {
        User user = getAuthenticatedUser();
        return messageRepository.findByUserId(user.getId());
    }
}
