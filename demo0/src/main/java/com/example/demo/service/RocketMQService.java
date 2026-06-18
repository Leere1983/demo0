package com.example.demo.service;

public interface RocketMQService {

    void sendMessage(String topic, String message);

    void sendMessageWithTag(String topic, String tag, String message);

    void sendOrderlyMessage(String topic, String message, String hashKey);
}