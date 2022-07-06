package com.example.rrlimiter.leakybucket;

import com.example.rrlimiter.tokenbucket.TokenBucketLimiter;
import com.example.rrlimiter.tokenbucket.TokenBucketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class LeakyBucketService {
    private static final Logger LOG = LoggerFactory.getLogger(LeakyBucketService.class);
    @Resource
    RedisTemplate redisTemplate;

    public boolean isLimit(String token) {
        LeakyBucketLimiter limiter = new LeakyBucketLimiter("leakybucket_limiter:" + token, "0", "0",
                "3", "1", redisTemplate);
        long result = limiter.acquire();
        LOG.info("------" + result);
        if (result == 0) {
            LOG.warn("flow control, limit!");
            return true;
        }
        return false;
    }
}
