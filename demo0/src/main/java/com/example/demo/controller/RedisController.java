package com.example.demo.controller;

import com.example.demo.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/redis")
@RequiredArgsConstructor
public class RedisController {

    private final RedisService redisService;

    @PostMapping("/string")
    public ResponseEntity<Void> setString(@RequestParam String key, @RequestParam String value, 
                                          @RequestParam(required = false) Long expireSeconds) {
        log.info("Setting string key: {}", key);
        if (expireSeconds != null) {
            redisService.setStringWithExpire(key, value, expireSeconds);
        } else {
            redisService.setString(key, value);
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/string/{key}")
    public ResponseEntity<String> getString(@PathVariable String key) {
        log.info("Getting string key: {}", key);
        String value = redisService.getString(key);
        return ResponseEntity.ok(value);
    }

    @DeleteMapping("/key/{key}")
    public ResponseEntity<Void> deleteKey(@PathVariable String key) {
        log.info("Deleting key: {}", key);
        redisService.deleteKey(key);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/list")
    public ResponseEntity<Void> setList(@RequestParam String key, @RequestBody List<String> values) {
        log.info("Setting list key: {}", key);
        redisService.setList(key, values);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list/{key}")
    public ResponseEntity<List<String>> getList(@PathVariable String key, 
                                                @RequestParam(defaultValue = "0") long start, 
                                                @RequestParam(defaultValue = "-1") long end) {
        log.info("Getting list key: {}", key);
        List<String> values = redisService.getList(key, start, end);
        return ResponseEntity.ok(values);
    }

    @PostMapping("/hash")
    public ResponseEntity<Void> setHash(@RequestParam String key, @RequestBody Map<String, String> hash) {
        log.info("Setting hash key: {}", key);
        redisService.setHash(key, hash);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/hash/{key}")
    public ResponseEntity<Map<String, String>> getHash(@PathVariable String key) {
        log.info("Getting hash key: {}", key);
        Map<String, String> hash = redisService.getHashAll(key);
        return ResponseEntity.ok(hash);
    }

    @PostMapping("/set")
    public ResponseEntity<Void> setSet(@RequestParam String key, @RequestBody Set<String> values) {
        log.info("Setting set key: {}", key);
        redisService.setSet(key, values);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/set/{key}")
    public ResponseEntity<Set<String>> getSet(@PathVariable String key) {
        log.info("Getting set key: {}", key);
        Set<String> values = redisService.getSet(key);
        return ResponseEntity.ok(values);
    }

    @PostMapping("/lock")
    public ResponseEntity<Long> acquireLock(@RequestParam String key, 
                                            @RequestParam(defaultValue = "30") long expireSeconds) {
        log.info("Acquiring lock for key: {}", key);
        Long value = redisService.tryLock(key, expireSeconds);
        if (value != null) {
            return ResponseEntity.ok(value);
        }
        return ResponseEntity.status(409).body(null);
    }

    @PostMapping("/unlock")
    public ResponseEntity<Boolean> releaseLock(@RequestParam String key, @RequestParam Long value) {
        log.info("Releasing lock for key: {}", key);
        boolean success = redisService.unlock(key, value);
        return ResponseEntity.ok(success);
    }
}