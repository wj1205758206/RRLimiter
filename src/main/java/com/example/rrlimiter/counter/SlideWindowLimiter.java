package com.example.rrlimiter.counter;

import com.example.rrlimiter.leakybucket.LeakyBucketLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SlideWindowLimiter {
    private static final Logger LOG = LoggerFactory.getLogger(SlideWindowLimiter.class);

    private String name;
    private String maxCount;
    private String period;
    private String timeout;
    private TimeUnit timeUnit;
    private RedisTemplate redisTemplate;

    public SlideWindowLimiter(String name, String maxCount, String period, String timeout, TimeUnit timeUnit, RedisTemplate redisTemplate) {
        this.name = name;
        this.maxCount = maxCount;
        this.period = period;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        this.redisTemplate = redisTemplate;

    }


    public long acquire() {
        return acquire(1);
    }

    public long acquire(int permits) {
        long executeTimes = 0;
        long ttl = timeUnit.toMillis(Long.parseLong(timeout)); // 毫秒
        long now = Instant.now().toEpochMilli();
        long expired = now - ttl;
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(
                    new ResourceScriptSource(new ClassPathResource("luaScript/slidewindow_script.lua")));
            redisScript.setResultType(Long.class);
            executeTimes = (long) redisTemplate.execute(redisScript, Arrays.asList(name), Long.toString(now),
                    Long.toString(ttl), Long.toString(expired), maxCount);
        } catch (Exception e) {
            LOG.error("[SlideWindowLimiter] acquire exception: " + e);
        }
        return executeTimes;
    }
}
