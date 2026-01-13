package com.pashcevich.data_unifier.adapter.mysql;

import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MySQLOrderAdapter {

    private final OrderRepository orderRepository;

    public List<UnifiedCustomerDto.OrderData> getOrdersByUserId(Long userId) {
        if (userId == null) {
            log.warn("Attempt to get orders with null user ID");
            return List.of();
        }

        try {
            log.info("Searching orders for user ID: {}", userId);

            List<OrderEntity> orders = orderRepository.findByUserId(userId);
            log.info("Found {} orders for user ID: {}", orders.size(), userId);

            if (orders.isEmpty()) {
                log.debug("No orders found for user ID: {}", userId);
                return List.of();
            }

            return orders.stream()
                    .map(this::convertToOrderData)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error while fetching orders for user ID: {}", userId, e);
            throw new OrderAdapterException("Failed to fetch orders for user: " + userId, e);
        }
    }

    private UnifiedCustomerDto.OrderData convertToOrderData(OrderEntity order) {
        try {
            if (order == null) {
                log.warn("Attempt to convert null OrderEntity to OrderData");
                return null;
            }

            UnifiedCustomerDto.OrderData orderData = new UnifiedCustomerDto.OrderData();
            orderData.setOrderId(order.getId());
            orderData.setAmount(order.getAmount());
            orderData.setStatus(order.getStatus());
            orderData.setCreatedAt(order.getCreatedAt());

            return orderData;
        } catch (Exception e) {
            log.error("Error while converting OrderEntity to OrderData: {}", order, e);
            return null;
        }
    }
}
