package com.qiusm.redis;

import cn.hutool.core.thread.ThreadUtil;
import com.qiusm.redis.service.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.redisson.api.RLock;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisTest extends RedisApplicationTests {

    @Resource
    private IRedisService redisService;

    /**
     * 将key缓存到redis上
     */
    @Test
    public void setKey() {
        RedisTemplate<Object, Object> redisTemplate = redisService.getRedisTemplate();
        redisTemplate.opsForValue().set("test", "moulu");
    }

    @Test
    public void getKey() {
        RedisTemplate<Object, Object> redisTemplate = redisService.getRedisTemplate();
        Object value = redisTemplate.opsForValue().get("test");
        log.info("value is '{}'", value);
    }

    /**
     * 目前测试证实加锁是有效的。但是存在一点问题<br>
     * 1. 当加锁【等待时间】小于【方法的执行时间】时，就会导致不加锁，然后出现死锁的状态。 <br>
     * 具体需要分析下{@link RLock#tryLock(long, long, TimeUnit)}方法 <br>
     */
    @Test
    public void redisLock() {
        ExecutorService threadPool = ThreadUtil.newExecutor(5);
        CountDownLatch countDownLatch = ThreadUtil.newCountDownLatch(5);

        for (int i = 0; i < 5; i++) {
            threadPool.submit(() -> {
                Object value = redisService.get("test");
                log.info("value is {}", value);
                countDownLatch.countDown();
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
