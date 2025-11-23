package com.pashcevich.data_unifier.adapter.mysql.repository;

import com.pashcevich.data_unifier.adapter.mysql.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, String> {
    List<OrderEntity> findByUserId(Long userId);

    List<OrderEntity> findByStatus(String status);

    List<OrderEntity> findByUserIdAndStatus(Long userId, String status);

    long countByUserId(Long userId);
}