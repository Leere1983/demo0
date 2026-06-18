package com.example.demo.service.impl;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Product;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.service.OrderService;
import com.example.demo.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final RedisService redisService;

    @Override
    @Transactional
    public Order createOrder(Long userId, List<Long> productIds, List<Integer> quantities) {
        log.info("Creating order for user {} with products {}", userId, productIds);
        
        if (productIds.size() != quantities.size()) {
            throw new RuntimeException("Product ids and quantities size mismatch");
        }

        Order order = Order.builder()
                .orderNo(generateOrderNo())
                .userId(userId)
                .totalAmount(BigDecimal.ZERO)
                .status(1)
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Integer quantity = quantities.get(i);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            if (product.getStock() < quantity) {
                throw new RuntimeException("Insufficient stock for product: " + productId);
            }

            int updated = productRepository.decreaseStock(productId, quantity);
            if (updated == 0) {
                throw new RuntimeException("Failed to decrease stock for product: " + productId);
            }

            OrderItem item = OrderItem.builder()
                    .productId(productId)
                    .quantity(quantity)
                    .unitPrice(product.getPrice())
                    .build();

            order.addItem(item);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalAmount(totalAmount);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public Order createOrderWithLock(Long userId, List<Long> productIds, List<Integer> quantities) {
        log.info("Creating order with distributed lock for user {}", userId);

        String lockKey = "order:create:" + userId;
        Long lockValue = redisService.tryLock(lockKey, 30);

        if (lockValue == null) {
            throw new RuntimeException("User is creating order, please wait");
        }

        try {
            return createOrder(userId, productIds, quantities);
        } finally {
            redisService.unlock(lockKey, lockValue);
        }
    }

    @Override
    @Transactional
    public Order updateOrderStatus(Long orderId, Integer status) {
        log.info("Updating order status for order {} to {}", orderId, status);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId) {
        log.info("Canceling order {}", orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != 1) {
            throw new RuntimeException("Cannot cancel order with status: " + order.getStatus());
        }

        for (OrderItem item : order.getItems()) {
            productRepository.increaseStock(item.getProductId(), item.getQuantity());
        }

        order.setStatus(5);
        orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(Integer status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByTimeRange(LocalDateTime start, LocalDateTime end) {
        return orderRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}