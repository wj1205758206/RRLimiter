package com.example.rrlimiter.tokenbucket;

import com.google.common.util.concurrent.RateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateLimiterConfig;
import org.redisson.api.RateType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.*;

public class TokenBucketLimiter {
    private static final Logger LOG = LoggerFactory.getLogger(TokenBucketLimiter.class);
    private static final String STORED_PERMITS = "storedPermits"; //当前存储的令牌数
    private static final String MAX_PERMITS = "maxPermits"; //最大可存储的令牌数，设置为限速器的qps
    private static final String STABLE_INTERVAL_MICROS = "stableIntervalMicros"; //多久产生一个令牌
    private static final String NEXT_FREE_TICKET_MICROS = "nextFreeTicketMicros"; //下一次可以获取令牌的时间点

    private String name;
    private double qps;
    private RedisTemplate redisTemplate;

    public TokenBucketLimiter(String name, double qps, RedisTemplate redisTemplate) {
        this.name = name;
        this.qps = qps;
        this.redisTemplate = redisTemplate;
        initLimiter();
    }

    private void initLimiter() {
        Map<String, String> limiter = new HashMap<>();
        limiter.put(STORED_PERMITS, Double.toString(qps));
        limiter.put(MAX_PERMITS, Double.toString(qps));
        limiter.put(STABLE_INTERVAL_MICROS, Double.toString(TimeUnit.SECONDS.toMicros(1L) / qps));
        limiter.put(NEXT_FREE_TICKET_MICROS, "0");
        redisTemplate.opsForHash().putAll(name, limiter);
    }

    public void setRate(double qps) {
        this.qps = qps;
        initLimiter();
    }

    public double acquire() {
        return acquire(1D);
    }

    public double acquire(double permits) {
        long nowMicros = MILLISECONDS.toMicros(System.currentTimeMillis());
        long waitMicros = 0;
        try {
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
            redisScript.setScriptSource(
                    new ResourceScriptSource(new ClassPathResource("luaScript/tokenbucket_script.lua")));
            redisScript.setResultType(Long.class);
            waitMicros = (long) redisTemplate.execute(redisScript, Arrays.asList(name),
                    "acquire", Double.toString(qps), Long.toString(nowMicros));
        } catch (Exception e) {
            LOG.error("[TokenBucketLimiter] acquire exception: " + e);
        }
        double wait = 1.0 * waitMicros / TimeUnit.SECONDS.toMicros(1L);
        if (waitMicros > 0) {
            sleepUninterruptibly(waitMicros, MILLISECONDS);
        }
        return wait;
    }

    public boolean tryAcquire() {
        return tryAcquire(1D, 0L, TimeUnit.MICROSECONDS);
    }

    public boolean tryAcquire(double qps, long timeout, TimeUnit unit) {
        long nowMicros = MILLISECONDS.toMicros(System.currentTimeMillis());
        long timeoutMicros = unit.toMicros(timeout);
        long waitMicros;

        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("luaScript/tokenbucket_script.lua")));
        redisScript.setResultType(Long.class);
        List<String> keys = Arrays.asList(name);
        waitMicros = (long) redisTemplate.execute(redisScript, keys,
                "tryAcquire", Double.toString(qps), Long.toString(nowMicros), Long.toString(timeoutMicros));
        LOG.error("-----------" + waitMicros);
        if (waitMicros < 0) {
            return false;
        }
        if (waitMicros > 0) {
            sleepUninterruptibly(waitMicros, MICROSECONDS);
        }
        return true;
    }


    /**
     * 计算阻塞等待时间
     *
     * @param sleepFor
     * @param unit
     */
    public static void sleepUninterruptibly(long sleepFor, TimeUnit unit) {
        boolean interrupted = false;
        try {
            long remainingNanos = unit.toNanos(sleepFor);
            long end = System.nanoTime() + remainingNanos;
            while (true) {
                try {
                    // TimeUnit.sleep() treats negative timeouts just like zero.
                    NANOSECONDS.sleep(remainingNanos);
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                    remainingNanos = end - System.nanoTime();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getQps() {
        return qps;
    }

    public void setQps(double qps) {
        this.qps = qps;
    }
}
