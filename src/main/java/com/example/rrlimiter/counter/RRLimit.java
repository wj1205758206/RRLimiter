package com.example.rrlimiter.counter;

import java.lang.annotation.*;

/**
 * 自定义限流注解
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RRLimit {
    String names() default "";

    String keys() default "";

    String prefixes() default "";

    int period();

    int counts();

    LimitType limitType() default LimitType.CUSTOM_KEY;
}
