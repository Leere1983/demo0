package com.example.demo0.interceptor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class RequestInterceptorTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    void testInterceptorPreHandle() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        System.out.println("=== Request Interceptor Test ===");
        System.out.println("Testing preHandle - requestId should be generated...");

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk());

        System.out.println("Request received - check logs for requestId");
    }

    @Test
    void testInterceptorPostHandle() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        System.out.println("Testing postHandle - response status should be logged...");

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());

        System.out.println("Response completed - check logs for response status");
    }

    @Test
    void testInterceptorAfterCompletion() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        System.out.println("Testing afterCompletion - duration should be calculated...");

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk());

        System.out.println("Request finished - check logs for duration");
    }

    @Test
    void testInterceptorWithError() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        System.out.println("Testing interceptor with error handling...");

        mockMvc.perform(get("/api/orders/invalid-id"))
                .andExpect(status().is4xxClientError());

        System.out.println("Error handled - check logs for error message");
    }
}