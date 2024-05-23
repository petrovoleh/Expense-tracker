package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Record;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.RecordRepository;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.expencetracker.webserver.component.security.jwt.JwtUtils;
import org.expencetracker.webserver.component.security.services.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/record")
public class RecordController {

    @Autowired
    private RecordRepository recordRepository;

    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<?> addRecord(@Valid @RequestBody Record record) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        record.setUserId(user.getId());
        Record savedRecord = recordRepository.save(record);
        return ResponseEntity.ok(savedRecord);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getRecordsByUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
            List<Record> records = recordRepository.findByUserId(user.getId());
        return ResponseEntity.ok(records);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecord(@PathVariable String id, @Valid @RequestBody Record recordDetails) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        Optional<Record> recordOptional = recordRepository.findById(id);

        if (recordOptional.isPresent()) {
            Record record = recordOptional.get();
            if(!record.getUserId().equals(user.getId())){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User unauthorized to change this record");
            }
            record.setCategory(recordDetails.getCategory());
            record.setDescription(recordDetails.getDescription());
            record.setValue(recordDetails.getValue());
            record.setUserId(recordDetails.getUserId());
            Record updatedRecord = recordRepository.save(record);
            return ResponseEntity.ok(updatedRecord);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecord(@PathVariable String id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }
        Optional<Record> recordOptional = recordRepository.findById(id);
        if (recordOptional.isPresent()) {
            Record record = recordOptional.get();
            if(!record.getUserId().equals(user.getId())){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User unauthorized to change this record");
            }
            recordRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
