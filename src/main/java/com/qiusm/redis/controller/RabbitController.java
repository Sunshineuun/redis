package com.qiusm.redis.controller;

import com.qiusm.redis.config.RabbitConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.MessageListenerContainer;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author qiushengming
 */
@Slf4j
@RequestMapping("rabbit")
@RestController
public class RabbitController {

    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    @GetMapping("/seed")
    public void seed() {
        for (int i = 0; i < 100; i++) {
            rabbitTemplate.convertAndSend(RabbitConfig.testDirectExchange, RabbitConfig.testRoutingKey, "test-msg" + i);
        }
    }


    /**
     * 开启监听
     */
    @GetMapping("/startListener/{listenerId}")
    public void startListener(@PathVariable String listenerId) {
        MessageListenerContainer container =
                rabbitListenerEndpointRegistry.getListenerContainer(listenerId);
        if (!container.isRunning()) {
            container.start();
            log.info("开启 RabbitMQ 监听. id: {}", listenerId);
        }
    }

    /**
     * 开启监听
     */
    @GetMapping("/stopListener/{listenerId}")
    public void stopListener(@PathVariable String listenerId) {
        MessageListenerContainer container =
                rabbitListenerEndpointRegistry.getListenerContainer(listenerId);
        if (container.isRunning()) {
            container.stop();
            log.info("关闭 RabbitMQ 监听. id: {}", listenerId);
        }
    }
}
