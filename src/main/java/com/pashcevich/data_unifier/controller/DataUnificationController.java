package com.pashcevich.data_unifier.controller;

import com.pashcevich.data_unifier.service.DataUnificationService;
import com.pashcevich.data_unifier.exception.DataUnificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.apache.kafka.common.requests.DeleteAclsResponse.log;

@RestController
@RequestMapping("/api/data-unification")
public class DataUnificationController {

    private static final Logger logger = LoggerFactory.getLogger(DataUnificationController.class);

    @Autowired
    private DataUnificationService dataUnificationService;

    @PostMapping("/process-all")
    public ResponseEntity<String> processAllData() {
        try {
            log.info("Starting complete data processing");
            dataUnificationService.processAllData();
            return ResponseEntity.ok("Data processing completed successfully");
        } catch (DataUnificationException e) {
            log.error("Data processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Data processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during data processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred");
        }
    }

    @PostMapping("/process-users")
    public ResponseEntity<String> processUsers() {
        try {
            log.info("Starting user data processing");
            dataUnificationService.processUserData();
            return ResponseEntity.ok("User data processing completed successfully");
        } catch (DataUnificationException e) {
            log.error("User data processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("User data processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during user data processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred");
        }
    }

    @PostMapping("/process-orders")
    public ResponseEntity<String> processOrders() {
        try {
            log.info("Starting order data processing");
            dataUnificationService.processOrderData();
            return ResponseEntity.ok("Order data processing completed successfully");
        } catch (DataUnificationException e) {
            log.error("Order data processing failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Order data processing failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during order data processing", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred");
        }
    }
}
