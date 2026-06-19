package com.example.demo0.aspect;

import com.example.demo0.service.OrderService;
import com.example.demo0.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PerformanceAspectTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Test
    void testPerformanceMeasurement() {
        System.out.println("=== Performance Aspect Test ===");
        System.out.println("Calling service methods to measure performance...");

        for (int i = 0; i < 5; i++) {
            orderService.findAll().size();
            userService.findAll().size();
        }

        System.out.println("Performance aspect test complete - check logs for statistics");
    }

    @Test
    void testRepositoryPerformance() {
        System.out.println("=== Repository Performance Test ===");
        System.out.println("Calling repository methods...");

        for (int i = 0; i < 10; i++) {
            orderService.findAll().size();
        }

        System.out.println("Repository performance test complete - check logs for statistics");
    }
}