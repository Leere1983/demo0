package com.example.demo0.service;
import com.example.demo0.entity.Order;
import com.example.demo0.entity.Product;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private static final String ORDER_CACHE_PREFIX = "order:";
    private static final String PRODUCT_CACHE_PREFIX = "product:";
    private static final String PRODUCT_LIST_KEY = "products:list";
    private static final String LOCK_PREFIX = "lock:";
    private static final long LOCK_EXPIRE_TIME = 30;
    private static final long CACHE_EXPIRE_TIME = 3600;
    public void cacheOrder(Order order) {
        try {
            String key = ORDER_CACHE_PREFIX + order.getId();
            String json = objectMapper.writeValueAsString(order);
            redisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            log.debug("Cached order: {}", order.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to cache order: {}", e.getMessage());
        }
    }
    public Order getCachedOrder(Long orderId) {
        String key = ORDER_CACHE_PREFIX + orderId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            try {
                return objectMapper.readValue(value.toString(), Order.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize order: {}", e.getMessage());
            }
        }
        return null;
    }
    public void removeOrder(Long orderId) {
        String key = ORDER_CACHE_PREFIX + orderId;
        redisTemplate.delete(key);
        log.debug("Removed order from cache: {}", orderId);
    }
    public void cacheProduct(Product product) {
        try {
            String key = PRODUCT_CACHE_PREFIX + product.getId();
            String json = objectMapper.writeValueAsString(product);
            redisTemplate.opsForValue().set(key, json, CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
            redisTemplate.opsForSet().add(PRODUCT_LIST_KEY, product.getId().toString());
            log.debug("Cached product: {}", product.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to cache product: {}", e.getMessage());
        }
    }
    public Product getCachedProduct(Long productId) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            try {
                return objectMapper.readValue(value.toString(), Product.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to deserialize product: {}", e.getMessage());
            }
        }
        return null;
    }
    public void removeProduct(Long productId) {
        String key = PRODUCT_CACHE_PREFIX + productId;
        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove(PRODUCT_LIST_KEY, productId.toString());
        log.debug("Removed product from cache: {}", productId);
    }
    public void cacheProductStock(Long productId, Integer stock) {
        String key = PRODUCT_CACHE_PREFIX + productId + ":stock";
        redisTemplate.opsForValue().set(key, stock.toString(), CACHE_EXPIRE_TIME, TimeUnit.SECONDS);
    }
    public Integer getCachedProductStock(Long productId) {
        String key = PRODUCT_CACHE_PREFIX + productId + ":stock";
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? Integer.parseInt(value.toString()) : null;
    }
    public void cacheUserSession(String sessionId, String userId) {
        String key = "session:" + sessionId;
        redisTemplate.opsForValue().set(key, userId, 1800, TimeUnit.SECONDS);
    }
    public String getUserIdFromSession(String sessionId) {
        String key = "session:" + sessionId;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }
    public void addToRecentProducts(Long userId, Long productId) {
        String key = "user:" + userId + ":recent";
        redisTemplate.opsForList().leftPush(key, productId.toString());
        redisTemplate.opsForList().trim(key, 0, 9);
    }
    public List<Object> getRecentProducts(Long userId) {
        String key = "user:" + userId + ":recent";
        return redisTemplate.opsForList().range(key, 0, 9);
    }
    public void addToCart(Long userId, Long productId, Integer quantity) {
        String key = "cart:" + userId;
        redisTemplate.opsForHash().put(key, productId.toString(), quantity.toString());
    }
    public Integer getCartItemQuantity(Long userId, Long productId) {
        String key = "cart:" + userId;
        Object value = redisTemplate.opsForHash().get(key, productId.toString());
        return value != null ? Integer.parseInt(value.toString()) : 0;
    }
    public void removeFromCart(Long userId, Long productId) {
        String key = "cart:" + userId;
        redisTemplate.opsForHash().delete(key, productId.toString());
    }
    public String acquireLock(String lockKey) {
        String key = LOCK_PREFIX + lockKey;
        String value = UUID.randomUUID().toString();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, LOCK_EXPIRE_TIME, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.debug("Acquired lock: {}", lockKey);
            return value;
        }
        return null;
    }
    public boolean releaseLock(String lockKey, String value) {
        String key = LOCK_PREFIX + lockKey;
        Object currentValue = redisTemplate.opsForValue().get(key);
        if (currentValue != null && currentValue.toString().equals(value)) {
            Boolean success = redisTemplate.delete(key);
            if (Boolean.TRUE.equals(success)) {
                log.debug("Released lock: {}", lockKey);
                return true;
            }
        }
        return false;
    }
    public boolean tryLock(String lockKey, long waitTime, TimeUnit timeUnit) throws InterruptedException {
        long endTime = System.currentTimeMillis() + timeUnit.toMillis(waitTime);
        while (System.currentTimeMillis() < endTime) {
            String value = acquireLock(lockKey);
            if (value != null) {
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }
    public void incrementCounter(String counterKey) {
        redisTemplate.opsForValue().increment(counterKey);
    }
    public Long getCounter(String counterKey) {
        Object value = redisTemplate.opsForValue().get(counterKey);
        return value != null ? Long.parseLong(value.toString()) : 0L;
    }
}