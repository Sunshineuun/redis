package com.qiusm.redis.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

/**
 * 监听队列 <br>
 * 基于注解的方式配置队列和交换机的关系 <br>
 * 注解在类上 <br>
 *
 * @author qiushengming
 */
@Slf4j
@Component
//@RabbitListener(bindings = @QueueBinding(
//        value = @Queue(value = "testQueue", durable = "true", autoDelete = "false"),
//        exchange = @Exchange(value = "topic_order_exchange", type = ExchangeTypes.TOPIC),
//        key = "#.#"
//))
@RabbitListener(queues = "testQueue")
public class ConsumerService {

    /**
     * 接收消息
     *
     * @param msg 消息
     */
    @RabbitHandler
    public void receiveMessage(String msg) {
        log.info("接收到消息：{}, topic", msg);
    }
}
