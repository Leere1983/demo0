package com.example.demo0.controller;
import com.example.demo0.entity.Order;
import com.example.demo0.entity.OrderItem;
import com.example.demo0.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private Map<String, Object> toOrderMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("userId", order.getUserId());
        map.put("orderNo", order.getOrderNo());
        map.put("totalAmount", order.getTotalAmount());
        map.put("status", order.getStatus());
        map.put("createdAt", order.getCreatedAt());
        map.put("updatedAt", order.getUpdatedAt());
        return map;
    }
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllOrders() {
        return ResponseEntity.ok(orderService.findAll().stream().map(this::toOrderMap).toList());
    }
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOrderById(@PathVariable Long id) {
        return orderService.findById(id)
                .map(this::toOrderMap)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}/items")
    public ResponseEntity<Map<String, Object>> getOrderWithItems(@PathVariable Long id) {
        return orderService.findByIdWithItems(id)
                .map(order -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", order.getId());
                    result.put("userId", order.getUserId());
                    result.put("orderNo", order.getOrderNo());
                    result.put("totalAmount", order.getTotalAmount());
                    result.put("status", order.getStatus());
                    result.put("createdAt", order.getCreatedAt());
                    result.put("updatedAt", order.getUpdatedAt());
                    List<Map<String, Object>> items = order.getOrderItems().stream()
                            .map(item -> {
                                Map<String, Object> itemMap = new HashMap<>();
                                itemMap.put("id", item.getId());
                                itemMap.put("orderId", item.getOrderId());
                                itemMap.put("productId", item.getProductId());
                                itemMap.put("quantity", item.getQuantity());
                                itemMap.put("unitPrice", item.getUnitPrice());
                                itemMap.put("createdAt", item.getCreatedAt());
                                return itemMap;
                            })
                            .toList();
                    result.put("orderItems", items);
                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getOrdersByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId).stream().map(this::toOrderMap).toList());
    }
    @GetMapping("/user/{userId}/total-amount")
    public ResponseEntity<Map<String, BigDecimal>> getTotalAmount(@PathVariable Long userId) {
        BigDecimal total = orderService.getTotalAmountByUserId(userId);
        return ResponseEntity.ok(Map.of("totalAmount", total != null ? total : BigDecimal.ZERO));
    }
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Map<String, Object>>> getOrdersByStatus(@PathVariable Integer status) {
        return ResponseEntity.ok(orderService.findByStatus(status).stream().map(this::toOrderMap).toList());
    }
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) request.get("items");
        List<OrderItem> orderItems = items.stream()
                .map(item -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProductId(Long.valueOf(item.get("productId").toString()));
                    orderItem.setQuantity(Integer.valueOf(item.get("quantity").toString()));
                    return orderItem;
                })
                .toList();
        return ResponseEntity.ok(toOrderMap(orderService.createOrder(userId, orderItems)));
    }
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        return ResponseEntity.ok(toOrderMap(orderService.updateStatus(id, request.get("status"))));
    }
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.ok().build();
    }
}