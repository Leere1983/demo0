package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @RequestParam Long userId,
            @RequestParam List<Long> productIds,
            @RequestParam List<Integer> quantities) {
        log.info("Received request to create order for user {}", userId);
        Order order = orderService.createOrder(userId, productIds, quantities);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/with-lock")
    public ResponseEntity<Order> createOrderWithLock(
            @RequestParam Long userId,
            @RequestParam List<Long> productIds,
            @RequestParam List<Integer> quantities) {
        log.info("Received request to create order with lock for user {}", userId);
        Order order = orderService.createOrderWithLock(userId, productIds, quantities);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        log.info("Received request to get order by id: {}", id);
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/order-no/{orderNo}")
    public ResponseEntity<Order> getOrderByOrderNo(@PathVariable String orderNo) {
        log.info("Received request to get order by orderNo: {}", orderNo);
        return orderService.getOrderByOrderNo(orderNo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status) {
        log.info("Received request to get orders");
        List<Order> orders;
        
        if (userId != null && status != null) {
            orders = orderService.getOrdersByUserId(userId);
            orders = orders.stream().filter(o -> o.getStatus().equals(status)).toList();
        } else if (userId != null) {
            orders = orderService.getOrdersByUserId(userId);
        } else if (status != null) {
            orders = orderService.getOrdersByStatus(status);
        } else {
            orders = orderService.getAllOrders();
        }
        
        return ResponseEntity.ok(orders);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable Long id, @RequestParam Integer status) {
        log.info("Received request to update order {} status to {}", id, status);
        Order order = orderService.updateOrderStatus(id, status);
        return ResponseEntity.ok(order);
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        log.info("Received request to cancel order {}", id);
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
}