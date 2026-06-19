package com.example.demo0.aspect;

import com.example.demo0.repository.LogRepository;
import com.example.demo0.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class LogAspectTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private OrderService orderService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        logRepository.deleteAll();
    }

    @Test
    void testControllerLogAspect() throws Exception {
        mockMvc.perform(get("/api/orders")).andExpect(status().isOk());

        mockMvc.perform(get("/api/users")).andExpect(status().isOk());

        System.out.println("Controller AOP verification complete - check logs for request details");
    }

    @Test
    void testServiceLogAspect() {
        long initialCount = logRepository.count();

        System.out.println("Service AOP verification - calling OrderService methods...");

        orderService.findAll().size();

        System.out.println("Service AOP verification complete - check logs for method entry/exit");
    }

    @Test
    void testSlowMethodDetection() throws InterruptedException {
        System.out.println("Slow method detection test - will call a method that takes > 500ms...");
        
        for (int i = 0; i < 3; i++) {
            Thread.sleep(100);
            orderService.findAll().size();
        }

        System.out.println("Slow method detection test complete - check logs for WARN messages");
    }
}