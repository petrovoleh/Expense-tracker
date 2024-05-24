package org.expencetracker.webserver.component.service;

import org.expencetracker.webserver.component.models.Record;
import org.expencetracker.webserver.component.models.User;
import org.expencetracker.webserver.component.repository.RecordRepository;
import org.expencetracker.webserver.component.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    public RecordService(RecordRepository recordRepository, UserRepository userRepository) {
        this.recordRepository = recordRepository;
        this.userRepository = userRepository;
    }

    /**
     * Retrieves the authenticated user from the security context.
     *
     * @return the authenticated user or null if not found.
     */
    public User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Adds a new record for the authenticated user.
     *
     * @param record the record to be added.
     * @return a ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<?> addRecord(Record record) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        record.setUserId(user.getId());
        Record savedRecord = recordRepository.save(record);
        return ResponseEntity.ok(savedRecord);
    }

    /**
     * Retrieves all records for the authenticated user.
     *
     * @return a ResponseEntity with the list of records.
     */
    public ResponseEntity<?> getRecordsByUserId() {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        List<Record> records = recordRepository.findByUserId(user.getId());
        return ResponseEntity.ok(records);
    }

    /**
     * Updates an existing record for the authenticated user.
     *
     * @param id the ID of the record to update.
     * @param recordDetails the updated record details.
     * @return a ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<?> updateRecord(String id, Record recordDetails) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Optional<Record> recordOptional = recordRepository.findById(id);
        if (recordOptional.isPresent()) {
            Record record = recordOptional.get();
            if (!record.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User unauthorized to change this record");
            }
            record.setCategory(recordDetails.getCategory());
            record.setDescription(recordDetails.getDescription());
            record.setValue(recordDetails.getValue());
            Record updatedRecord = recordRepository.save(record);
            return ResponseEntity.ok(updatedRecord);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes an existing record for the authenticated user.
     *
     * @param id the ID of the record to delete.
     * @return a ResponseEntity indicating the result of the operation.
     */
    public ResponseEntity<?> deleteRecord(String id) {
        User user = getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
        }

        Optional<Record> recordOptional = recordRepository.findById(id);
        if (recordOptional.isPresent()) {
            Record record = recordOptional.get();
            if (!record.getUserId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User unauthorized to delete this record");
            }
            recordRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
