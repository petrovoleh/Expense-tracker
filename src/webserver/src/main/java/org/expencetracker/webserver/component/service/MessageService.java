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

    /**
     * Retrieves the currently authenticated user.
     *
     * @return the authenticated User
     * @throws IllegalStateException if the user is not found
     */
    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found: " + username));
    }

    /**
     * Saves a message associated with the currently authenticated user.
     *
     * @param message the message to save
     * @return the saved Message
     */
    public Message saveMessage(Message message) {
        User user = getAuthenticatedUser();
        message.setUserId(user.getId());
        return messageRepository.save(message);
    }

    /**
     * Retrieves messages by date range for the currently authenticated user.
     *
     * @param from the start of the date range
     * @param to the end of the date range
     * @return a list of messages within the specified date range
     */
    public List<Message> getMessagesByDateRange(LocalDateTime from, LocalDateTime to) {
        User user = getAuthenticatedUser();
        return messageRepository.findByUserIdAndDateBetween(user.getId(), from, to);
    }

    /**
     * Retrieves all messages for the currently authenticated user.
     *
     * @return a list of all messages for the authenticated user
     */
    public List<Message> getAllMessagesByAuthenticatedUser() {
        User user = getAuthenticatedUser();
        return messageRepository.findByUserId(user.getId());
    }
}
