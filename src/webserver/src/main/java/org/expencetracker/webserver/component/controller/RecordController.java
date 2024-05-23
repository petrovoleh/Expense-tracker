package org.expencetracker.webserver.component.controller;

import jakarta.validation.Valid;
import org.expencetracker.webserver.component.models.Record;
import org.expencetracker.webserver.component.service.RecordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/record")
public class RecordController {

    private final RecordService recordService;

    public RecordController(RecordService recordService) {
        this.recordService = recordService;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addRecord(@Valid @RequestBody Record record) {
        return recordService.addRecord(record);
    }

    @GetMapping("/user")
    public ResponseEntity<?> getRecordsByUserId() {
        return recordService.getRecordsByUserId();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecord(@PathVariable String id, @Valid @RequestBody Record recordDetails) {
        return recordService.updateRecord(id, recordDetails);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecord(@PathVariable String id) {
        return recordService.deleteRecord(id);
    }
}
