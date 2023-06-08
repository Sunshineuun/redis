package com.qiusm.redis.service;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 消费者定义。注解在方法上。
 *
 * @author qiushengming
 */
@Slf4j
//@Component
public class ConsumerMethodService {

    /**
     * 接收单个消息
     *
     * @param object  发送消息时的对象，对象相对于，rabbit会帮助解析成对应的对象
     * @param channel channel
     * @param message message
     */
    @RabbitHandler
    @RabbitListener(queues = "testQueue", ackMode = "MANUAL", concurrency = "1", autoStartup = "false", id =
            "testQueueReceiver")
    public void testQueueReceiver(String object, Channel channel, Message message) {
        try {
            log.info("接收到消息：object: {}", object);
            // 可以将消息已字符串的格式展示，一般调用的Object.toString方法
            // message.getBodyContentAsString()
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }
    }

    /**
     * 批量消费的实现 <br>
     * 1. 如果是对象，在发送消息的时候都将其序列化为JSON字符串。<br>
     * 接收时，将其转为String，再将其转为对象处理。<br>
     * 2. 需要考虑，是否有直接序列化为对象的方法。<br> TODO
     *
     * @param messages messages
     * @param channel  channel
     */
    @RabbitHandler
    @RabbitListener(queues = "testQueue", ackMode = "MANUAL", concurrency = "1", autoStartup = "false", id =
            "testQueueReceiverBatch")
    public void testQueueReceiverBatch(List<Message> messages, Channel channel) {
        try {
            for (Message message : messages) {
                log.info("接收到消息：message: {}", new String(message.getBody()));
                // 可以将消息已字符串的格式展示，一般调用的Object.toString方法
                // message.getBodyContentAsString()
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            }
        } catch (Exception e) {
            log.error("{}", e.getMessage());
        }
    }
}
