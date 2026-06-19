package com.example.demo0.controller;

import com.example.demo0.service.RocketMQProducerService;
import lombok.RequiredArgsConstructor;
import org.apache.rocketmq.client.producer.SendResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final RocketMQProducerService rocketMQProducerService;

    @PostMapping("/order")
    public ResponseEntity<SendResult> sendOrderMessage(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String orderNo = request.get("orderNo").toString();
        SendResult result = rocketMQProducerService.sendOrderMessage(orderId, userId, orderNo);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/order/async")
    public ResponseEntity<Map<String, Object>> sendOrderMessageAsync(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        Long userId = Long.valueOf(request.get("userId").toString());
        String orderNo = request.get("orderNo").toString();
        rocketMQProducerService.sendOrderMessageAsync(orderId, userId, orderNo);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "异步消息已发送");
        response.put("orderId", orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/log")
    public ResponseEntity<Map<String, Object>> sendLogMessage(@RequestBody Map<String, String> request) {
        String action = request.get("action");
        String description = request.get("description");
        String ipAddress = request.get("ipAddress");
        rocketMQProducerService.sendLogMessage(action, description, ipAddress);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "日志消息已发送");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delay")
    public ResponseEntity<Map<String, Object>> sendDelayMessage(@RequestBody Map<String, Object> request) {
        Long orderId = Long.valueOf(request.get("orderId").toString());
        int delayLevel = Integer.valueOf(request.get("delayLevel").toString());
        rocketMQProducerService.sendDelayMessage(orderId, delayLevel);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "延迟消息已发送");
        response.put("orderId", orderId);
        response.put("delayLevel", delayLevel);
        return ResponseEntity.ok(response);
    }
}