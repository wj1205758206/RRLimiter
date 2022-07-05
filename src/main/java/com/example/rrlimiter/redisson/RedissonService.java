package com.example.rrlimiter.redisson;


import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class RedissonService {

    @Resource
    RedissonClient redissonClient;

    public boolean isLimit(String token) {
        RRateLimiter limiter = redissonClient.getRateLimiter("redisson_limiter:" + token);
        limiter.trySetRate(RateType.OVERALL, 1, 2, RateIntervalUnit.SECONDS);
        limiter.expire(10, TimeUnit.SECONDS);
        if (limiter.tryAcquire(1)) {
            return false;
        }
        return true;
    }
}
