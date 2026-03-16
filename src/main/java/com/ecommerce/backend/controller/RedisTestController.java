package com.ecommerce.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/test/redis")
public class RedisTestController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * Set a value in Redis with a 10-minute expiration.
     */
    @GetMapping("/set")
    public ResponseEntity<Map<String, String>> setRedisValue(
            @RequestParam String key, 
            @RequestParam String value) {
        
        redisTemplate.opsForValue().set(key, value, 10, TimeUnit.MINUTES);
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "Success");
        response.put("message", "Value set for key: " + key);
        return ResponseEntity.ok(response);
    }

    /**
     * Get a value from Redis by key.
     */
    @GetMapping("/get")
    public ResponseEntity<Map<String, Object>> getRedisValue(@RequestParam String key) {
        Object value = redisTemplate.opsForValue().get(key);
        
        Map<String, Object> response = new HashMap<>();
        if (value != null) {
            response.put("status", "Success");
            response.put("key", key);
            response.put("value", value);
        } else {
            response.put("status", "Not Found");
            response.put("message", "No value found for key: " + key);
        }
        return ResponseEntity.ok(response);
    }
}
