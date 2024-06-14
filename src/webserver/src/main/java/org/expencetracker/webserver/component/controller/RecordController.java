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

    /**
     * Endpoint to add a new record for the authenticated user.
     *
     * @param record the record to be added.
     * @return a ResponseEntity indicating the result of the operation.
     */
    @PostMapping("/add")
    public ResponseEntity<?> addRecord(@RequestBody Record record) {
        return recordService.addRecord(record);
    }

    /**
     * Endpoint to get all records for the authenticated user.
     *
     * @return a ResponseEntity with the list of records.
     */
    @GetMapping("/user")
    public ResponseEntity<?> getRecordsByUserId() {
        return recordService.getRecordsByUserId();
    }

    /**
     * Endpoint to update an existing record for the authenticated user.
     *
     * @param id the ID of the record to update.
     * @param recordDetails the updated record details.
     * @return a ResponseEntity indicating the result of the update operation.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRecord(@PathVariable String id, @Valid @RequestBody Record recordDetails) {
        return recordService.updateRecord(id, recordDetails);
    }

    /**
     * Endpoint to delete an existing record for the authenticated user.
     *
     * @param id the ID of the record to delete.
     * @return a ResponseEntity indicating the result of the delete operation.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecord(@PathVariable String id) {
        return recordService.deleteRecord(id);
    }
}
