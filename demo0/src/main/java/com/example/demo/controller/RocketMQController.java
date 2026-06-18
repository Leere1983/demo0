package com.example.demo.controller;

import com.example.demo.service.RocketMQService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rocketmq")
@RequiredArgsConstructor
public class RocketMQController {

    private final RocketMQService rocketMQService;

    @PostMapping("/send")
    public ResponseEntity<Void> sendMessage(@RequestParam String topic, @RequestBody String message) {
        log.info("Received request to send message to topic: {}", topic);
        rocketMQService.sendMessage(topic, message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-with-tag")
    public ResponseEntity<Void> sendMessageWithTag(@RequestParam String topic, 
                                                   @RequestParam String tag, 
                                                   @RequestBody String message) {
        log.info("Received request to send message to topic: {} with tag: {}", topic, tag);
        rocketMQService.sendMessageWithTag(topic, tag, message);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/send-orderly")
    public ResponseEntity<Void> sendOrderlyMessage(@RequestParam String topic, 
                                                   @RequestBody String message, 
                                                   @RequestParam String hashKey) {
        log.info("Received request to send orderly message to topic: {} with hashKey: {}", topic, hashKey);
        rocketMQService.sendOrderlyMessage(topic, message, hashKey);
        return ResponseEntity.ok().build();
    }
}