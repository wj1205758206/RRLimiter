package com.example.rrlimiter;


import com.example.rrlimiter.counter.FixWindowService;
import com.example.rrlimiter.counter.SlideWindowService;
import com.example.rrlimiter.leakybucket.LeakyBucketService;
import com.example.rrlimiter.redisson.RedissonService;
import com.example.rrlimiter.tokenbucket.TokenBucketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class RRLimiterController {
    private static final Logger LOG = LoggerFactory.getLogger("RedissonController");

    @Resource
    RedissonService redissonService;
    @Resource
    FixWindowService fixWindowService;
    @Resource
    TokenBucketService tokenBucketService;
    @Resource
    LeakyBucketService leakyBucketService;
    @Resource
    SlideWindowService slideWindowService;

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

    @GetMapping("/fixWindowLimiter")
    public String testFixWindowLimiter() {
        String token = "123456";
        boolean access = fixWindowService.isLimit(token);
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

    @GetMapping("/leakyBucketLimiter")
    public String testLeakyBucketLimiter() {
        String token = "123456";
        boolean access = leakyBucketService.isLimit(token);
        if (!access) {
            LOG.info("token: {} is not limit", token);
            return "token:" + token + " is not limit";
        }
        LOG.info("token: {} is not limit", token);
        return "token:" + token + " is limit!!!";
    }

    @GetMapping("/slideWindowLimiter")
    public String testsLideWindowLimiter() {
        String token = "123456";
        boolean access = slideWindowService.isLimit(token);
        if (!access) {
            LOG.info("token: {} is not limit", token);
            return "token:" + token + " is not limit";
        }
        LOG.info("token: {} is not limit", token);
        return "token:" + token + " is limit!!!";
    }
}
