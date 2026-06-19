package com.example.demo0.service;

import com.example.demo0.entity.Order;
import com.example.demo0.entity.OrderItem;
import com.example.demo0.entity.Product;
import com.example.demo0.repository.OrderItemRepository;
import com.example.demo0.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final RedisService redisService;

    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByIdWithItems(Long id) {
        return orderRepository.findByIdWithItems(id);
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByOrderNo(String orderNo) {
        return orderRepository.findByOrderNo(orderNo);
    }

    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(Integer status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalAmountByUserId(Long userId) {
        return orderRepository.getTotalAmountByUserId(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    public Order createOrder(Long userId, List<OrderItem> items) {
        String orderNo = generateOrderNo();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : items) {
            Product product = productService.findById(item.getProductId())
                    .orElseThrow(() -> new RuntimeException("商品不存在 " + item.getProductId()));
            boolean success = productService.decreaseStock(product.getId(), item.getQuantity());
            if (!success) {
                throw new RuntimeException("库存不足: " + product.getName());
            }
            BigDecimal itemAmount = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            totalAmount = totalAmount.add(itemAmount);
            item.setUnitPrice(product.getPrice());
        }
        Order order = new Order();
        order.setUserId(userId);
        order.setOrderNo(orderNo);
        order.setTotalAmount(totalAmount);
        order.setStatus(0);
        Order savedOrder = orderRepository.save(order);
        for (OrderItem item : items) {
            item.setOrderId(savedOrder.getId());
            orderItemRepository.save(item);
        }
        redisService.cacheOrder(savedOrder);
        return savedOrder;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public Order updateStatus(Long orderId, Integer status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        redisService.cacheOrder(updatedOrder);
        return updatedOrder;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (order.getStatus() != 0) {
            throw new RuntimeException("只能取消待支付的订单");
        }
        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        for (OrderItem item : items) {
            productService.increaseStock(item.getProductId(), item.getQuantity());
        }
        order.setStatus(4);
        orderRepository.save(order);
        redisService.removeOrder(orderId);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return "ORD" + timestamp + random;
    }
}