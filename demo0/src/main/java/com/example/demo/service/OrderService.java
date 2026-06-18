package com.example.demo.service;

import com.example.demo.entity.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderService {

    Order createOrder(Long userId, List<Long> productIds, List<Integer> quantities);

    Order createOrderWithLock(Long userId, List<Long> productIds, List<Integer> quantities);

    Order updateOrderStatus(Long orderId, Integer status);

    void cancelOrder(Long orderId);

    Optional<Order> getOrderById(Long id);

    Optional<Order> getOrderByOrderNo(String orderNo);

    List<Order> getOrdersByUserId(Long userId);

    List<Order> getOrdersByStatus(Integer status);

    List<Order> getOrdersByTimeRange(LocalDateTime start, LocalDateTime end);

List<Order> getAllOrders();
}