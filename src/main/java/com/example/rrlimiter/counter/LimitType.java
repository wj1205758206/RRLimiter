package com.example.rrlimiter.counter;

/**
 * 限流类型
 */
public enum LimitType {

    /**
     * 自定义限流KEY
     */
    CUSTOM_KEY,

    /**
     * 调用者IP限流
     */
    IP;
}
