package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Message;
import org.expencetracker.webserver.component.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return ResponseEntity.ok(savedMessage);
    }

    @GetMapping("/user/{userId}/from/{fromDate}/to/{toDate}")
    public ResponseEntity<List<Message>> getMessagesByUserIdAndDateRange(
            @PathVariable String userId,
            @PathVariable String fromDate,
            @PathVariable String toDate) {
        LocalDateTime from = LocalDateTime.parse(fromDate);
        LocalDateTime to = LocalDateTime.parse(toDate);
        List<Message> messages = messageService.getMessagesByUserIdAndDateRange(userId, from, to);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Message>> getAllMessagesByUserId(@PathVariable String userId) {
        List<Message> messages = messageService.getAllMessagesByUserId(userId);
        return ResponseEntity.ok(messages);
    }
}
