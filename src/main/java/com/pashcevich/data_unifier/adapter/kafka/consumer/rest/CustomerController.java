package com.pashcevich.data_unifier.adapter.kafka.consumer.rest;

import com.pashcevich.data_unifier.adapter.kafka.consumer.UniversalConsumerAdapter;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final UniversalConsumerAdapter universalConsumerAdapter;

    @GetMapping
    public ResponseEntity<List<UnifiedCustomerDto>> getAllCustomers() {
        List<UnifiedCustomerDto> customers = universalConsumerAdapter.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<UnifiedCustomerDto> getCustomerById(@PathVariable Long userId) {
        UnifiedCustomerDto customer = universalConsumerAdapter.getCustomerById(userId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    @GetMapping("/{userId}/exists")
    public ResponseEntity<Boolean> checkCustomerExists(@PathVariable Long userId) {
        boolean exists = universalConsumerAdapter.customerExists(userId);
        return ResponseEntity.ok(exists);
    }

}
