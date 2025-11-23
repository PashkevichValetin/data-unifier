package com.pashcevich.data_unifier.adapter.mysql;

import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import com.pashcevich.data_unifier.adapter.kafka.producer.dto.UnifiedCustomerDto;
import com.pashcevich.data_unifier.adapter.mysql.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MySQLOrderAdapter {

    private final OrderRepository orderRepository;

    public List<UnifiedCustomerDto> fetchCustomersWithOrders() {
        try {
            List<OrderEntity> allOrders = orderRepository.findAll();
            log.info("Fetched {} orders from MySQL", allOrders.size());

            Map<String, List<OrderEntity>> ordersByEmail = allOrders.stream()
                    .collect(Collectors.groupingBy(order ->
                            order.getCustomerEmail() != null ? order.getCustomerEmail().trim().toLowerCase() : "unknown"
                    ));

            return ordersByEmail.entrySet().stream()
                    .map(entry -> createUnifiedCustomer(entry.getKey(), entry.getValue()))
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error fetching orders from MySQL", e);
            return List.of();
        }
    }

    private UnifiedCustomerDto createUnifiedCustomer(String email, List<OrderEntity> orders) {
        if (orders == null || orders.isEmpty()) {
            return null;
        }

        OrderEntity firstOrder = orders.get(0);
        Long userId = firstOrder.getUserId();

        UnifiedCustomerDto dto = new UnifiedCustomerDto();
        dto.setCustomerId("MY_" + userId);
        dto.setUserId(userId);

        String customerName = firstOrder.getCustomerName();
        if (customerName == null || customerName.trim().isEmpty()) {
            customerName = generateCustomerName(email, userId);
        } else {
            customerName = customerName.trim();
        }

        dto.setName(customerName);
        dto.setEmail(email != null ? email : "user_" + userId + "@example.com");
        dto.setRegistrationDate(firstOrder.getOrderDate() != null ?
                firstOrder.getOrderDate().minusMonths(1) : java.time.LocalDateTime.now().minusMonths(1));
        dto.setDataSource("mysql");
        dto.setUnifiedTimestamp(java.time.LocalDateTime.now());

        List<UnifiedCustomerDto.OrderDto> orderDtos = orders.stream()
                .map(this::convertToOrderDto)
                .collect(Collectors.toList());

        dto.setOrders(orderDtos);
        return dto;
    }

    private String generateCustomerName(String email, Long userId) {
        if (email != null && !email.equals("unknown")) {
            String namePart = email.split("@")[0];
            String cleanName = namePart.replaceAll("[^a-zA-Zа-яА-Я]", "");
            if (!cleanName.isEmpty()) {
                return cleanName.substring(0, 1).toUpperCase() + cleanName.substring(1).toLowerCase();
            }
        }
        return "Customer_" + userId;
    }

    private UnifiedCustomerDto.OrderDto convertToOrderDto(OrderEntity order) {
        UnifiedCustomerDto.OrderDto orderDto = new UnifiedCustomerDto.OrderDto();
        orderDto.setOrderId(String.valueOf(order.getOrderId()));
        orderDto.setAmount(order.getAmount());
        orderDto.setStatus(order.getStatus());
        orderDto.setOrderDate(order.getOrderDate());
        return orderDto;
    }
}