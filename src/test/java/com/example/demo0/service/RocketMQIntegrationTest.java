package com.example.demo0.service;

import com.example.demo0.entity.Log;
import com.example.demo0.entity.Order;
import com.example.demo0.entity.User;
import com.example.demo0.repository.LogRepository;
import com.example.demo0.repository.OrderRepository;
import com.example.demo0.repository.UserRepository;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RocketMQIntegrationTest {

    @Autowired
    private RocketMQProducerService producerService;

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisService redisService;

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        orderRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void testSendOrderMessageSync() throws InterruptedException {
        SendResult result = producerService.sendOrderMessage(1L, 100L, "ORD202401010001");

        assertNotNull(result);
        assertEquals(SendStatus.SEND_OK, result.getSendStatus());
        System.out.println("Send result: " + result);

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void testSendOrderMessageAsync() throws InterruptedException {
        producerService.sendOrderMessageAsync(2L, 200L, "ORD202401010002");

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void testSendLogMessage() throws InterruptedException {
        producerService.sendLogMessage("TEST_ACTION", "Test description", "127.0.0.1");

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void testSendDelayMessage() throws InterruptedException {
        User user = createTestUser(300L);
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderNo("ORD202401010003");
        order.setTotalAmount(new BigDecimal("50.00"));
        order.setStatus(0);
        Order savedOrder = orderRepository.save(order);

        producerService.sendDelayMessage(savedOrder.getId(), 1);

        TimeUnit.SECONDS.sleep(5);
    }

    @Test
    void testSendMessageWithTag() throws InterruptedException {
        SendResult result = producerService.sendMessageWithTag("order-topic", "test", 
                "{\"test\": \"data\"}");

        assertNotNull(result);
        assertEquals(SendStatus.SEND_OK, result.getSendStatus());

        TimeUnit.SECONDS.sleep(2);
    }

    @Test
    void testOrderConsumerProcess() throws InterruptedException {
        User user = createTestUser(400L);
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderNo("ORD202401010004");
        order.setTotalAmount(new BigDecimal("200.00"));
        order.setStatus(0);
        Order savedOrder = orderRepository.save(order);

        SendResult result = producerService.sendOrderMessage(savedOrder.getId(), savedOrder.getUserId(), savedOrder.getOrderNo());
        assertEquals(SendStatus.SEND_OK, result.getSendStatus());

        TimeUnit.SECONDS.sleep(5);

        Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(1, updatedOrder.getStatus());

        List<Log> logs = logRepository.findByAction("ORDER_PAY");
        assertTrue(logs.size() >= 1, "Expected at least 1 ORDER_PAY log, got " + logs.size());
    }

    @Test
    void testLogConsumerProcess() throws InterruptedException {
        producerService.sendLogMessage("TEST_LOG", "Test log message", "192.168.1.1");

        List<Log> logs = waitForLogs("TEST_LOG", 1, 10);
        assertNotNull(logs);
        assertTrue(logs.size() >= 1, "Expected at least 1 TEST_LOG entry, got " + logs.size());
        assertEquals("Test log message", logs.get(0).getDescription());
        assertEquals("192.168.1.1", logs.get(0).getIpAddress());
    }

    @Test
    void testDelayConsumerAutoCancel() throws InterruptedException {
        User user = createTestUser(500L);
        Order order = new Order();
        order.setUserId(user.getId());
        order.setOrderNo("ORD202401010005");
        order.setTotalAmount(new BigDecimal("100.00"));
        order.setStatus(0);
        Order savedOrder = orderRepository.save(order);

        producerService.sendDelayMessage(savedOrder.getId(), 1);

        TimeUnit.SECONDS.sleep(5);

        Order updatedOrder = orderRepository.findById(savedOrder.getId()).orElse(null);
        assertNotNull(updatedOrder);
        assertEquals(4, updatedOrder.getStatus());

        List<Log> logs = logRepository.findByAction("ORDER_AUTO_CANCEL");
        assertTrue(logs.size() >= 1, "Expected at least 1 ORDER_AUTO_CANCEL log, got " + logs.size());
    }

    @Test
    void testDirectSendAndReceive() throws InterruptedException {
        String message = "{\"testField\": \"testValue\", \"timestamp\": " + System.currentTimeMillis() + "}";
        rocketMQTemplate.syncSend("order-topic", message);

        TimeUnit.SECONDS.sleep(2);
    }

    private User createTestUser(Long userId) {
        User user = new User();
        user.setUsername("testUser" + userId);
        user.setEmail("test" + userId + "@example.com");
        user.setPassword("password");
        return userRepository.save(user);
    }

    private List<Log> waitForLogs(String action, int expectedCount, int maxRetries) throws InterruptedException {
        for (int i = 0; i < maxRetries; i++) {
            List<Log> logs = logRepository.findByAction(action);
            if (logs.size() >= expectedCount) {
                return logs;
            }
            TimeUnit.SECONDS.sleep(1);
        }
        return logRepository.findByAction(action);
    }
}