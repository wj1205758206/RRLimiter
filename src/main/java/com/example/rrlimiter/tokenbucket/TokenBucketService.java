package com.example.rrlimiter.tokenbucket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Service
public class TokenBucketService {
    private static final Logger LOG = LoggerFactory.getLogger(TokenBucketService.class);
    @Resource
    RedisTemplate redisTemplate;

    public boolean isLimit(String token) {
        TokenBucketLimiter limiter = new TokenBucketLimiter("tokenbucket_limiter:" + token, 1, redisTemplate);
        boolean success = limiter.tryAcquire();
        LOG.info("------" + success);
        if (!success) {
            LOG.warn("flow control, limit!");
            return true;
        }
        return false;
    }
}
