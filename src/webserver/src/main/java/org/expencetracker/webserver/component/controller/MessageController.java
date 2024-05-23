package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Message;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.service.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/post")
    public ResponseEntity<Message> postMessage(@Valid @RequestBody Message message) {
        Message savedMessage = messageService.saveMessage(message);
        if (savedMessage != null) {
            return ResponseEntity.ok(savedMessage);
        } else {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
    }

    @GetMapping("/from/{fromDate}/to/{toDate}")
    public ResponseEntity<List<Message>> getMessagesByDateRange(
            @PathVariable String fromDate,
            @PathVariable String toDate) {
        User user = messageService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
        LocalDateTime from = LocalDateTime.parse(fromDate);
        LocalDateTime to = LocalDateTime.parse(toDate);
        List<Message> messages = messageService.getMessagesByUserIdAndDateRange(user.getId(), from, to);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user")
    public ResponseEntity<List<Message>> getAllMessagesByUserId() {
        User user = messageService.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(401).body(null); // Unauthorized
        }
        List<Message> messages = messageService.getAllMessagesByUserId(user.getId());
        return ResponseEntity.ok(messages);
    }
}
