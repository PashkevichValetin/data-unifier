package com.pashcevich.data_unifier.adapter.kafka.consumer.rest;

import com.pashcevich.data_unifier.adapter.kafka.consumer.UniversalConsumerAdapter;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final UniversalConsumerAdapter universalConsumerAdapter;

    /**
     * Получить всех customers
     */
    @GetMapping
    public ResponseEntity<List<UnifiedCustomerDto>> getAllCustomers() {
        List<UnifiedCustomerDto> customers = universalConsumerAdapter.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * Получить customer по customerId (например: "PG_123", "MY_456")
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<UnifiedCustomerDto> getCustomerById(@PathVariable String customerId) {
        UnifiedCustomerDto customer = universalConsumerAdapter.getCustomerById(customerId);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    /**
     * Получить customer по userId и источнику данных
     */
    @GetMapping("/source/{dataSource}/user/{userId}")
    public ResponseEntity<UnifiedCustomerDto> getCustomerByUserIdAndSource(
            @PathVariable String dataSource,
            @PathVariable Long userId) {
        UnifiedCustomerDto customer = universalConsumerAdapter.getCustomerByUserId(userId, dataSource);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }

    /**
     * Поиск customers по email
     */
    @GetMapping("/search/email")
    public ResponseEntity<List<UnifiedCustomerDto>> searchCustomersByEmail(
            @RequestParam String email) {
        List<UnifiedCustomerDto> customers = universalConsumerAdapter.findCustomersByEmail(email);
        return ResponseEntity.ok(customers);
    }

    /**
     * Поиск customers по имени
     */
    @GetMapping("/search/name")
    public ResponseEntity<List<UnifiedCustomerDto>> searchCustomersByName(
            @RequestParam String name) {
        List<UnifiedCustomerDto> customers = universalConsumerAdapter.findCustomersByName(name);
        return ResponseEntity.ok(customers);
    }

    /**
     * Получить customers по источнику данных
     */
    @GetMapping("/source/{dataSource}")
    public ResponseEntity<List<UnifiedCustomerDto>> getCustomersByDataSource(
            @PathVariable String dataSource) {
        List<UnifiedCustomerDto> customers = universalConsumerAdapter.getCustomersByDataSource(dataSource);
        return ResponseEntity.ok(customers);
    }

    /**
     * Проверить существование customer
     */
    @GetMapping("/{customerId}/exists")
    public ResponseEntity<Boolean> checkCustomerExists(@PathVariable String customerId) {
        boolean exists = universalConsumerAdapter.customerExists(customerId);
        return ResponseEntity.ok(exists);
    }

    /**
     * Получить общее количество customers
     */
    @GetMapping("/count")
    public ResponseEntity<Long> getCustomerCount() {
        long count = universalConsumerAdapter.getCustomerCount();
        return ResponseEntity.ok(count);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Customer API is running");
    }
}