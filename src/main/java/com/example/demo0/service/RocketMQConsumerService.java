package com.example.demo0.service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.demo0.entity.Log;
import com.example.demo0.repository.LogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Service;

@Slf4j
public class RocketMQConsumerService {

    @Service
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = "order-topic", consumerGroup = "order-consumer-group")
    public static class OrderConsumer implements RocketMQListener<String> {
        private final LogRepository logRepository;
        private final OrderService orderService;
        private final RedisService redisService;

        @Override
        public void onMessage(String message) {
            try {
                JSONObject jsonMessage = JSON.parseObject(message);
                Long orderId = jsonMessage.getLong("orderId");
                Long userId = jsonMessage.getLong("userId");
                String orderNo = jsonMessage.getString("orderNo");
                log.info("Received order message: orderId={}, userId={}, orderNo={}", 
                        orderId, userId, orderNo);
                orderService.updateStatus(orderId, 1);
                redisService.incrementCounter("order:count");
                Log logEntry = new Log();
                logEntry.setUserId(userId);
                logEntry.setAction("ORDER_PAY");
                logEntry.setDescription("订单支付成功: " + orderNo);
                logRepository.save(logEntry);
                log.info("Processed order message successfully: orderId={}", orderId);
            } catch (Exception e) {
                log.error("Failed to process order message: {}", e.getMessage(), e);
                throw e;
            }
        }
    }

    @Service
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = "log-topic", consumerGroup = "log-consumer-group")
    public static class LogConsumer implements RocketMQListener<String> {
        private final LogRepository logRepository;

        @Override
        public void onMessage(String message) {
            try {
                JSONObject jsonMessage = JSON.parseObject(message);
                String action = jsonMessage.getString("action");
                String description = jsonMessage.getString("description");
                String ipAddress = jsonMessage.getString("ipAddress");
                log.info("Received log message: action={}, description={}", action, description);
                Log logEntry = new Log();
                logEntry.setAction(action);
                logEntry.setDescription(description);
                logEntry.setIpAddress(ipAddress);
                logRepository.save(logEntry);
                log.info("Saved log entry: action={}", action);
            } catch (Exception e) {
                log.error("Failed to process log message: {}", e.getMessage(), e);
            }
        }
    }

    @Service
    @RequiredArgsConstructor
    @RocketMQMessageListener(topic = "delay-topic", consumerGroup = "delay-consumer-group")
    public static class DelayConsumer implements RocketMQListener<String> {
        private final LogRepository logRepository;
        private final OrderService orderService;

        @Override
        public void onMessage(String message) {
            try {
                JSONObject jsonMessage = JSON.parseObject(message);
                Long orderId = jsonMessage.getLong("orderId");
                int delayLevel = jsonMessage.getInteger("delayLevel");
                log.info("Received delay message: orderId={}, delayLevel={}", orderId, delayLevel);
                orderService.findById(orderId).ifPresent(order -> {
                    if (order.getStatus() == 0) {
                        log.warn("Order {} is still pending, canceling...", orderId);
                        try {
                            orderService.cancelOrder(orderId);
                            Log logEntry = new Log();
                            logEntry.setUserId(order.getUserId());
                            logEntry.setAction("ORDER_AUTO_CANCEL");
                            logEntry.setDescription("订单超时自动取消: " + order.getOrderNo());
                            logRepository.save(logEntry);
                        } catch (Exception e) {
                            log.error("Failed to cancel order: {}", e.getMessage());
                        }
                    }
                });
            } catch (Exception e) {
                log.error("Failed to process delay message: {}", e.getMessage(), e);
            }
        }
    }
}