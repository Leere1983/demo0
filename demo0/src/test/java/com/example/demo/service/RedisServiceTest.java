package com.example.demo.service;

import com.example.demo.service.impl.RedisServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedisServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RedisServiceImpl redisService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testSetAndGetString() {
        when(valueOperations.set(anyString(), any())).thenReturn(true);
        when(valueOperations.get("testKey")).thenReturn("testValue");

        redisService.setString("testKey", "testValue");
        String result = redisService.getString("testKey");

        assertEquals("testValue", result);
        verify(valueOperations, times(1)).set("testKey", "testValue");
        verify(valueOperations, times(1)).get("testKey");
    }

    @Test
    void testGetNonExistentKey() {
        when(valueOperations.get("nonExistentKey")).thenReturn(null);

        String result = redisService.getString("nonExistentKey");

        assertNull(result);
    }

    @Test
    void testExistsKey() {
        when(redisTemplate.hasKey("existingKey")).thenReturn(true);
        when(redisTemplate.hasKey("nonExistingKey")).thenReturn(false);

        assertTrue(redisService.existsKey("existingKey"));
        assertFalse(redisService.existsKey("nonExistingKey"));
    }

    @Test
    void testDeleteKey() {
        when(redisTemplate.delete("testKey")).thenReturn(true);

        redisService.deleteKey("testKey");

        verify(redisTemplate, times(1)).delete("testKey");
    }

    @Test
    void testTryLockSuccess() {
        when(valueOperations.setIfAbsent(anyString(), anyLong(), anyLong(), any())).thenReturn(true);

        Long result = redisService.tryLock("lockKey", 30);

        assertNotNull(result);
        verify(valueOperations, times(1)).setIfAbsent(eq("lockKey"), anyLong(), eq(30L), any());
    }

    @Test
    void testTryLockFailure() {
        when(valueOperations.setIfAbsent(anyString(), anyLong(), anyLong(), any())).thenReturn(false);

        Long result = redisService.tryLock("lockKey", 30);

        assertNull(result);
    }
}