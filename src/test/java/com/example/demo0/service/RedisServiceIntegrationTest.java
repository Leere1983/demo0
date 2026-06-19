package com.example.demo0.service;

import com.example.demo0.entity.Order;
import com.example.demo0.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RedisServiceIntegrationTest {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private Order testOrder;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setUserId(1L);
        testOrder.setOrderNo("ORD202401010001");
        testOrder.setTotalAmount(new BigDecimal("100.00"));
        testOrder.setStatus(1);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setName("Test Product");
        testProduct.setPrice(new BigDecimal("50.00"));
        testProduct.setStock(100);
        testProduct.setCategory("Electronics");
    }

    @Test
    void testCacheAndGetOrder() {
        redisService.cacheOrder(testOrder);

        Order cached = redisService.getCachedOrder(1L);

        assertNotNull(cached);
        assertEquals(testOrder.getId(), cached.getId());
        assertEquals(testOrder.getOrderNo(), cached.getOrderNo());
        assertEquals(testOrder.getTotalAmount(), cached.getTotalAmount());
        assertEquals(testOrder.getStatus(), cached.getStatus());
    }

    @Test
    void testGetCachedOrderNotFound() {
        Order cached = redisService.getCachedOrder(999L);

        assertNull(cached);
    }

    @Test
    void testRemoveOrder() {
        redisService.cacheOrder(testOrder);

        Order cachedBefore = redisService.getCachedOrder(1L);
        assertNotNull(cachedBefore);

        redisService.removeOrder(1L);

        Order cachedAfter = redisService.getCachedOrder(1L);
        assertNull(cachedAfter);
    }

    @Test
    void testCacheAndGetProduct() {
        redisService.cacheProduct(testProduct);

        Product cached = redisService.getCachedProduct(1L);

        assertNotNull(cached);
        assertEquals(testProduct.getId(), cached.getId());
        assertEquals(testProduct.getName(), cached.getName());
        assertEquals(testProduct.getPrice(), cached.getPrice());
        assertEquals(testProduct.getStock(), cached.getStock());
        assertEquals(testProduct.getCategory(), cached.getCategory());
    }

    @Test
    void testGetCachedProductNotFound() {
        Product cached = redisService.getCachedProduct(999L);

        assertNull(cached);
    }

    @Test
    void testRemoveProduct() {
        redisService.cacheProduct(testProduct);

        Product cachedBefore = redisService.getCachedProduct(1L);
        assertNotNull(cachedBefore);

        redisService.removeProduct(1L);

        Product cachedAfter = redisService.getCachedProduct(1L);
        assertNull(cachedAfter);
    }

    @Test
    void testCacheAndGetProductStock() {
        redisService.cacheProductStock(1L, 50);

        Integer stock = redisService.getCachedProductStock(1L);

        assertNotNull(stock);
        assertEquals(50, stock);
    }

    @Test
    void testGetCachedProductStockNotFound() {
        Integer stock = redisService.getCachedProductStock(999L);

        assertNull(stock);
    }

    @Test
    void testCacheAndGetUserSession() {
        redisService.cacheUserSession("session-abc", "user-123");

        String userId = redisService.getUserIdFromSession("session-abc");

        assertNotNull(userId);
        assertEquals("user-123", userId);
    }

    @Test
    void testGetUserIdFromSessionNotFound() {
        String userId = redisService.getUserIdFromSession("non-existent");

        assertNull(userId);
    }

    @Test
    void testAddToRecentProducts() {
        redisService.addToRecentProducts(1L, 100L);
        redisService.addToRecentProducts(1L, 200L);
        redisService.addToRecentProducts(1L, 300L);

        List<Object> recent = redisService.getRecentProducts(1L);

        assertNotNull(recent);
        assertEquals(3, recent.size());
        assertEquals("300", recent.get(0));
        assertEquals("200", recent.get(1));
        assertEquals("100", recent.get(2));
    }

    @Test
    void testGetRecentProductsEmpty() {
        List<Object> recent = redisService.getRecentProducts(999L);

        assertNotNull(recent);
        assertTrue(recent.isEmpty());
    }

    @Test
    void testRecentProductsLimit() {
        for (int i = 0; i < 15; i++) {
            redisService.addToRecentProducts(1L, (long) i);
        }

        List<Object> recent = redisService.getRecentProducts(1L);

        assertNotNull(recent);
        assertEquals(10, recent.size());
    }

    @Test
    void testAddToCart() {
        redisService.addToCart(1L, 100L, 5);

        Integer quantity = redisService.getCartItemQuantity(1L, 100L);

        assertEquals(5, quantity);
    }

    @Test
    void testGetCartItemQuantity() {
        redisService.addToCart(1L, 100L, 3);
        redisService.addToCart(1L, 200L, 7);

        assertEquals(3, redisService.getCartItemQuantity(1L, 100L));
        assertEquals(7, redisService.getCartItemQuantity(1L, 200L));
        assertEquals(0, redisService.getCartItemQuantity(1L, 300L));
    }

    @Test
    void testRemoveFromCart() {
        redisService.addToCart(1L, 100L, 5);

        assertEquals(5, redisService.getCartItemQuantity(1L, 100L));

        redisService.removeFromCart(1L, 100L);

        assertEquals(0, redisService.getCartItemQuantity(1L, 100L));
    }

    @Test
    void testAcquireLock() {
        String lock = redisService.acquireLock("test-lock");

        assertNotNull(lock);
        assertEquals(36, lock.length());
    }

    @Test
    void testAcquireLockFailed() {
        String lock1 = redisService.acquireLock("test-lock");
        assertNotNull(lock1);

        String lock2 = redisService.acquireLock("test-lock");
        assertNull(lock2);
    }

    @Test
    void testReleaseLock() {
        String lock = redisService.acquireLock("test-lock");
        assertNotNull(lock);

        boolean released = redisService.releaseLock("test-lock", lock);
        assertTrue(released);

        String lockAfter = redisService.acquireLock("test-lock");
        assertNotNull(lockAfter);
    }

    @Test
    void testReleaseLockFailed() {
        String lock = redisService.acquireLock("test-lock");
        assertNotNull(lock);

        boolean released = redisService.releaseLock("test-lock", "wrong-value");
        assertFalse(released);

        String lockAfter = redisService.acquireLock("test-lock");
        assertNull(lockAfter);
    }

    @Test
    void testTryLockSuccess() throws InterruptedException {
        boolean locked = redisService.tryLock("test-lock", 1, TimeUnit.SECONDS);

        assertTrue(locked);
    }

    @Test
    void testTryLockTimeout() throws InterruptedException {
        String lock = redisService.acquireLock("test-lock");
        assertNotNull(lock);

        boolean locked = redisService.tryLock("test-lock", 100, TimeUnit.MILLISECONDS);

        assertFalse(locked);
    }

    @Test
    void testIncrementCounter() {
        redisService.incrementCounter("test-counter");
        redisService.incrementCounter("test-counter");
        redisService.incrementCounter("test-counter");

        Long count = redisService.getCounter("test-counter");

        assertEquals(3L, count);
    }

    @Test
    void testGetCounterZero() {
        Long count = redisService.getCounter("non-existent-counter");

        assertEquals(0L, count);
    }
}