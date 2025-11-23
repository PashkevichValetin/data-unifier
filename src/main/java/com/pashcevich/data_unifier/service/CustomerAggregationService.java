package com.pashcevich.data_unifier.service;

import com.pashcevich.data_unifier.adapter.kafka.consumer.UniversalConsumerAdapter;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerAggregationService {

    private final UniversalConsumerAdapter universalConsumerAdapter;

    public CustomerStatistics getCustomerStatistics() {
        List<UnifiedCustomerDto> customers = universalConsumerAdapter.getAllCustomers();

        long totalCustomers = customers.size();
        long customersWithOrders = customers.stream()
                .filter(c -> c.getOrders() != null && !c.getOrders().isEmpty())
                .count();

        BigDecimal totalRevenue = customers.stream()
                .flatMap(c -> Optional.ofNullable(c.getOrders()).orElse(List.of()).stream())
                .map(UnifiedCustomerDto.OrderDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CustomerStatistics(totalCustomers, customersWithOrders, totalRevenue);
    }

    public CustomerDetail getCustomerDetail(String customerId) {
        UnifiedCustomerDto customer = universalConsumerAdapter.getCustomerById(customerId);
        if (customer == null) {
            return null;
        }

        List<UnifiedCustomerDto.OrderDto> orders = Optional.ofNullable(customer.getOrders())
                .orElse(List.of());

        BigDecimal totalSpent = orders.stream()
                .map(UnifiedCustomerDto.OrderDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedOrders = orders.stream()
                .filter(order -> "completed".equals(order.getStatus()))
                .count();

        return new CustomerDetail(customer, orders.size(), totalSpent, completedOrders);
    }

    public CustomerDetail getCustomerDetailBySource(Long userId, String dataSource) {
        UnifiedCustomerDto customer = universalConsumerAdapter.getCustomerByUserId(userId, dataSource);
        if (customer == null) {
            return null;
        }

        List<UnifiedCustomerDto.OrderDto> orders = Optional.ofNullable(customer.getOrders())
                .orElse(List.of());

        BigDecimal totalSpent = orders.stream()
                .map(UnifiedCustomerDto.OrderDto::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedOrders = orders.stream()
                .filter(order -> "completed".equals(order.getStatus()))
                .count();

        return new CustomerDetail(customer, orders.size(), totalSpent, completedOrders);
    }

    public List<UnifiedCustomerDto> findCustomersWithMinOrders(int minOrders) {
        return universalConsumerAdapter.getAllCustomers().stream()
                .filter(c -> Optional.ofNullable(c.getOrders()).orElse(List.of()).size() >= minOrders)
                .toList();
    }

    public List<UnifiedCustomerDto> findHighValueCustomers(BigDecimal minTotalSpent) {
        return universalConsumerAdapter.getAllCustomers().stream()
                .filter(c -> {
                    BigDecimal total = Optional.ofNullable(c.getOrders()).orElse(List.of()).stream()
                            .map(UnifiedCustomerDto.OrderDto::getAmount)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return total.compareTo(minTotalSpent) >= 0;
                })
                .toList();
    }

    public record CustomerStatistics(
            long totalCustomers,
            long customersWithOrders,
            BigDecimal totalRevenue
    ) {}

    public record CustomerDetail(
            UnifiedCustomerDto customer,
            int orderCount,
            BigDecimal totalSpent,
            long completedOrders
    ) {}
}