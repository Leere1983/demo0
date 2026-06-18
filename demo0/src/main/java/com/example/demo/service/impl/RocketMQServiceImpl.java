package com.example.demo.service.impl;

import com.example.demo.service.RocketMQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RocketMQServiceImpl implements RocketMQService {

    private final RocketMQTemplate rocketMQTemplate;

    @Override
    public void sendMessage(String topic, String message) {
        log.info("Sending message to topic: {}", topic);
        rocketMQTemplate.send(topic, MessageBuilder.withPayload(message).build());
        log.info("Message sent successfully to topic: {}", topic);
    }

    @Override
    public void sendMessageWithTag(String topic, String tag, String message) {
        log.info("Sending message to topic: {} with tag: {}", topic, tag);
        String destination = topic + ":" + tag;
        rocketMQTemplate.send(destination, MessageBuilder.withPayload(message).build());
        log.info("Message sent successfully to topic: {} with tag: {}", topic, tag);
    }

    @Override
    public void sendOrderlyMessage(String topic, String message, String hashKey) {
        log.info("Sending orderly message to topic: {} with hashKey: {}", topic, hashKey);
        rocketMQTemplate.syncSendOrderly(topic, MessageBuilder.withPayload(message).build(), hashKey);
        log.info("Orderly message sent successfully to topic: {}", topic);
    }
}