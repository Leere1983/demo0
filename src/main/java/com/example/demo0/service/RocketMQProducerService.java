package com.example.demo0.service;
import com.alibaba.fastjson.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
@Service
@RequiredArgsConstructor
@Slf4j
public class RocketMQProducerService {
    private final RocketMQTemplate rocketMQTemplate;
    private static final String ORDER_TOPIC = "order-topic";
    private static final String LOG_TOPIC = "log-topic";
    private static final String DELAY_TOPIC = "delay-topic";
    public SendResult sendOrderMessage(Long orderId, Long userId, String orderNo) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("userId", userId);
        message.put("orderNo", orderNo);
        message.put("timestamp", System.currentTimeMillis());
        String jsonMessage = JSON.toJSONString(message);
        SendResult result = rocketMQTemplate.syncSend(ORDER_TOPIC, jsonMessage);
        log.info("Sent order message: orderId={}, result={}", orderId, result.getSendStatus());
        return result;
    }
    public void sendOrderMessageAsync(Long orderId, Long userId, String orderNo) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("userId", userId);
        message.put("orderNo", orderNo);
        message.put("timestamp", System.currentTimeMillis());
        String jsonMessage = JSON.toJSONString(message);
        rocketMQTemplate.asyncSend(ORDER_TOPIC, jsonMessage, new SendCallback() {
            @Override
            public void onSuccess(SendResult sendResult) {
                log.info("Async send order message success: orderId={}, status={}", 
                        orderId, sendResult.getSendStatus());
            }
            @Override
            public void onException(Throwable throwable) {
                log.error("Async send order message failed: orderId={}, error={}", 
                        orderId, throwable.getMessage());
            }
        });
    }
    public void sendLogMessage(String action, String description, String ipAddress) {
        Map<String, Object> message = new HashMap<>();
        message.put("action", action);
        message.put("description", description);
        message.put("ipAddress", ipAddress);
        message.put("timestamp", System.currentTimeMillis());
        String jsonMessage = JSON.toJSONString(message);
        rocketMQTemplate.sendOneWay(LOG_TOPIC, jsonMessage);
        log.info("Sent log message: action={}", action);
    }
    public void sendDelayMessage(Long orderId, int delayLevel) {
        Map<String, Object> message = new HashMap<>();
        message.put("orderId", orderId);
        message.put("delayLevel", delayLevel);
        message.put("timestamp", System.currentTimeMillis());
        String jsonMessage = JSON.toJSONString(message);
        rocketMQTemplate.syncSend(DELAY_TOPIC, 
                org.springframework.messaging.support.MessageBuilder.withPayload(jsonMessage).build(), 
                3000L, 
                delayLevel);
        log.info("Sent delay message: orderId={}, delayLevel={}", orderId, delayLevel);
    }
    public SendResult sendMessageWithTag(String topic, String tag, Object message) {
        String jsonMessage = JSON.toJSONString(message);
        SendResult result = rocketMQTemplate.syncSend(topic + ":" + tag, jsonMessage);
        log.info("Sent message with tag: topic={}, tag={}", topic, tag);
        return result;
    }
}