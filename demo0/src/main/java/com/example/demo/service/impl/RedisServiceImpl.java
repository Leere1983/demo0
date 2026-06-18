package com.example.demo.service.impl;

import com.example.demo.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisServiceImpl implements RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void setString(String key, String value) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value);
    }

    @Override
    public void setStringWithExpire(String key, String value, long expireSeconds) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        ops.set(key, value, expireSeconds, TimeUnit.SECONDS);
    }

    @Override
    public String getString(String key) {
        ValueOperations<String, Object> ops = redisTemplate.opsForValue();
        Object value = ops.get(key);
        return value != null ? value.toString() : null;
    }

    @Override
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public boolean existsKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    @Override
    public void setList(String key, List<String> values) {
        BoundListOperations<String, Object> ops = redisTemplate.boundListOps(key);
        ops.delete();
        ops.rightPushAll(values.toArray());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getList(String key, long start, long end) {
        BoundListOperations<String, Object> ops = redisTemplate.boundListOps(key);
        List<Object> result = ops.range(start, end);
        return result != null ? result.stream().map(Object::toString).toList() : List.of();
    }

    @Override
    public void addToList(String key, String value) {
        BoundListOperations<String, Object> ops = redisTemplate.boundListOps(key);
        ops.rightPush(value);
    }

    @Override
    public long getListSize(String key) {
        BoundListOperations<String, Object> ops = redisTemplate.boundListOps(key);
        Long size = ops.size();
        return size != null ? size : 0;
    }

    @Override
    public void setHash(String key, Map<String, String> hash) {
        redisTemplate.opsForHash().putAll(key, hash);
    }

    @Override
    public void putHash(String key, String field, String value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    @Override
    public String getHash(String key, String field) {
        Object value = redisTemplate.opsForHash().get(key, field);
        return value != null ? value.toString() : null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, String> getHashAll(String key) {
        Map<Object, Object> result = redisTemplate.opsForHash().entries(key);
        return result != null ? 
                result.entrySet().stream()
                        .collect(java.util.stream.Collectors.toMap(
                                e -> e.getKey().toString(), 
                                e -> e.getValue().toString())) : 
                Map.of();
    }

    @Override
    public void deleteHashField(String key, String field) {
        redisTemplate.opsForHash().delete(key, field);
    }

    @Override
    public void setSet(String key, Set<String> values) {
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps(key);
        ops.delete();
        ops.add(values.toArray());
    }

    @Override
    public void addToSet(String key, String value) {
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps(key);
        ops.add(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> getSet(String key) {
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps(key);
        Set<Object> result = ops.members();
        return result != null ? 
                result.stream().map(Object::toString).collect(java.util.stream.Collectors.toSet()) : 
                Set.of();
    }

    @Override
    public boolean isMemberOfSet(String key, String value) {
        BoundSetOperations<String, Object> ops = redisTemplate.boundSetOps(key);
        Boolean result = ops.isMember(value);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public void sortedSetAdd(String key, String value, double score) {
        BoundZSetOperations<String, Object> ops = redisTemplate.boundZSetOps(key);
        ops.add(value, score);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<String> sortedSetRange(String key, long start, long end) {
        BoundZSetOperations<String, Object> ops = redisTemplate.boundZSetOps(key);
        Set<Object> result = ops.range(start, end);
        return result != null ? 
                result.stream().map(Object::toString).collect(java.util.stream.Collectors.toSet()) : 
                Set.of();
    }

    @Override
    public Long tryLock(String key, long expireSeconds) {
        Long value = Thread.currentThread().getId();
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, expireSeconds, TimeUnit.SECONDS);
        if (Boolean.TRUE.equals(success)) {
            log.info("Acquired lock for key: {} by thread: {}", key, value);
            return value;
        }
        log.info("Failed to acquire lock for key: {}", key);
        return null;
    }

    @Override
    public boolean unlock(String key, Long value) {
        String luaScript = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long result = redisTemplate.execute(
                (org.springframework.data.redis.core.script.RedisScript<Long>) script -> script.getSha1(),
                List.of(key),
                value.toString()
        );
        boolean success = result != null && result > 0;
        if (success) {
            log.info("Released lock for key: {} by thread: {}", key, value);
        }
        return success;
    }
}