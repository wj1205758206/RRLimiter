package com.example.rrlimiter.counter;

import com.example.rrlimiter.tokenbucket.TokenBucketLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Service
public class SlideWindowService {
    private static final Logger LOG = LoggerFactory.getLogger(SlideWindowService.class);
    @Resource
    RedisTemplate redisTemplate;

    public boolean isLimit(String token) {
        SlideWindowLimiter limiter = new SlideWindowLimiter("slidewindow_limiter:" + token, "1",
                "1", "0", TimeUnit.SECONDS, redisTemplate);
        long acquire = limiter.acquire();
        LOG.info("------" + acquire);
        if (acquire == 0) {
            LOG.warn("flow control, limit!");
            return true;
        }
        return false;
    }
}
