package com.qiusm.redis.service;

import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author qiushengming
 */
public interface IRedisService {
    RedisTemplate<Object, Object> getRedisTemplate();

    Object get(String key);
}
