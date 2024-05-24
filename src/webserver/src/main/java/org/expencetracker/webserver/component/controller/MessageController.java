package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Message;
import org.expencetracker.webserver.component.service.MessageService;
import org.springframework.format.annotation.DateTimeFormat;
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

    /**
     * Endpoint to post a new message.
     *
     * @param message the message to post
     * @return ResponseEntity containing the saved message
     */
    @PostMapping("/post")
    public ResponseEntity<Message> postMessage(@Valid @RequestBody Message message) {
        Message savedMessage = messageService.saveMessage(message);
        return ResponseEntity.ok(savedMessage);
    }

    /**
     * Endpoint to retrieve messages within a specified date range.
     *
     * @param fromDate the start of the date range
     * @param toDate the end of the date range
     * @return ResponseEntity containing a list of messages within the date range
     */
    @GetMapping("/from/{fromDate}/to/{toDate}")
    public ResponseEntity<List<Message>> getMessagesByDateRange(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        List<Message> messages = messageService.getMessagesByDateRange(fromDate, toDate);
        return ResponseEntity.ok(messages);
    }

    /**
     * Endpoint to retrieve all messages for the currently authenticated user.
     *
     * @return ResponseEntity containing a list of all messages for the authenticated user
     */
    @GetMapping("/user")
    public ResponseEntity<List<Message>> getAllMessagesByUserId() {
        List<Message> messages = messageService.getAllMessagesByAuthenticatedUser();
        return ResponseEntity.ok(messages);
    }
}
