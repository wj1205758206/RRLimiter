package com.example.rrlimiter.leakybucket;

import com.example.rrlimiter.tokenbucket.TokenBucketLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class LeakyBucketLimiter {
    private static final Logger LOG = LoggerFactory.getLogger(LeakyBucketLimiter.class);

    private static final String LAST_LEAKING_TIME = "lastLeakingTime";
    private static final String REMAINING_CAPACITY = "remainingCapacity";
    private static final String CAPACITY = "capacity";
    private static final String LEAKING_RATE = "leakingRate";

    private String name;
    private String lastLeakingTime;
    private String remainingCapacity;
    private String capacity;
    private String leakingRate;
    private RedisTemplate redisTemplate;


    public LeakyBucketLimiter(String name, String lastLeakingTime, String remainingCapacity, String capacity, String leakingRate, RedisTemplate redisTemplate) {
        this.name = name;
        this.lastLeakingTime = lastLeakingTime;
        this.remainingCapacity = remainingCapacity;
        this.capacity = capacity;
        this.leakingRate = leakingRate;
        this.redisTemplate = redisTemplate;
        initLimiter();
    }

    private void initLimiter() {
        Map<String, String> limiter = new HashMap<>();
        limiter.put(LAST_LEAKING_TIME, lastLeakingTime);
        limiter.put(REMAINING_CAPACITY, remainingCapacity);
        limiter.put(CAPACITY, capacity);
        limiter.put(LEAKING_RATE, leakingRate);
        redisTemplate.opsForHash().putAll(name, limiter);
    }

    public void setRate(int rate) {
        this.leakingRate = Integer.toString(rate);
        initLimiter();
    }

    public long acquire() {
        return acquire(1);
    }

    public long acquire(int permits) {
        long result = 0;
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(
                    new ResourceScriptSource(new ClassPathResource("luaScript/leakybucket_script.lua")));
            redisScript.setResultType(Long.class);
            result = (long) redisTemplate.execute(redisScript, Arrays.asList(name));
        } catch (Exception e) {
            LOG.error("[LeakyBucketLimiter] acquire exception: " + e);
        }
        return result;
    }
}
