package com.example.rrlimiter.redisson;


import com.example.rrlimiter.counter.CounterService;
import com.example.rrlimiter.tokenbucket.TokenBucketService;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class RedissonController {
    private static final Logger LOG = LoggerFactory.getLogger("RedissonController");

    @Resource
    RedissonService redissonService;
    @Resource
    CounterService counterService;
    @Resource
    TokenBucketService tokenBucketService;

    @GetMapping("/redissonLimiter")
    public String testRedisson() {
        String token = "123456";
        boolean access = redissonService.isLimit(token);
        if (!access) {
            LOG.info("token: {} is not limit", token);
            return "token:" + token + " is not limit";
        }
        LOG.info("token: {} is not limit", token);
        return "token:" + token + " is limit!!!";
    }

    @GetMapping("/counterLimiter")
    public String testCounter() {
        String token = "123456";
        boolean access = counterService.isLimit(token);
        if (!access) {
            LOG.info("token: {} is not limit", token);
            return "token:" + token + " is not limit";
        }
        LOG.info("token: {} is not limit", token);
        return "token:" + token + " is limit!!!";
    }

    @GetMapping("/tokenBucketLimiter")
    public String testTokenBucketLimiter() {
        String token = "123456";
        boolean access = tokenBucketService.isLimit(token);
        if (!access) {
            LOG.info("token: {} is not limit", token);
            return "token:" + token + " is not limit";
        }
        LOG.info("token: {} is not limit", token);
        return "token:" + token + " is limit!!!";
    }
}
