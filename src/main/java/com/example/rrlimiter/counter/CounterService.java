package com.example.rrlimiter.counter;

import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
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
import java.util.concurrent.TimeUnit;

@Service
public class CounterService {
    private static final Logger LOG = LoggerFactory.getLogger(CounterService.class);
    @Resource
    RedisTemplate redisTemplate;

    public boolean isLimit(String token) {
        List<String> keys = Arrays.asList(token);
        int counts = 1;
        int period = 5;

        DefaultRedisScript<Boolean> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(
                new ResourceScriptSource(new ClassPathResource("luaScript/counter_script.lua")));
        redisScript.setResultType(Boolean.class);
        boolean success = (boolean) redisTemplate.execute(redisScript, keys, Integer.toString(counts), Integer.toString(period));
        if (!success) {
            LOG.warn("flow control, limit!");
            return true;
        }
        return false;
    }
}
