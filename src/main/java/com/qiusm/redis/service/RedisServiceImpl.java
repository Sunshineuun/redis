package com.qiusm.redis.service;

import cn.hutool.core.thread.ThreadUtil;
import com.github.hsindumas.redis.lock.annotation.Lock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * redis
 *
 * @author qiushengming
 */
@Slf4j
@Service
public class RedisServiceImpl implements IRedisService, InitializingBean {
    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public RedisTemplate<Object, Object> getRedisTemplate() {
        return redisTemplate;
    }

    @Lock(keys = "redis_lock")
    @Override
    public Object get(String key) {
        ThreadUtil.sleep(5, TimeUnit.SECONDS);
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取指定前缀的一系列key <br>
     * 使用scan命令代替keys, Redis是单线程处理，keys命令在KEY数量较多时，<br>
     * 操作效率极低【时间复杂度为O(N)】，该命令一旦执行会严重阻塞线上其它命令的正常请求<br>
     *
     * @param keyPrefix keyPrefix
     */
    public Set<String> keys(String keyPrefix) {
        try {
            return redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
                Set<String> binaryKeys = new HashSet<>();
                ScanOptions scanOptions = ScanOptions.scanOptions()
                        .match(keyPrefix)
                        .count(10000).build();
                Cursor<byte[]> cursor = connection.scan(scanOptions);
                while (cursor.hasNext()) {
                    String key = new String(cursor.next());
                    binaryKeys.add(key);
                    if (binaryKeys.size() > 10000) {
                        break;
                    }
                }

                return binaryKeys;
            });
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("create RedisServiceImpl ");
    }
}
