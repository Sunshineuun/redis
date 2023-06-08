package com.qiusm.redis.controller;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author qiushengming
 */
@RestController
public class KafkaProducer {
    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 发送消息
     *
     * @param normalMessage
     */
    @GetMapping("/kafka/normal/{message}")
    public void sendMessage(@PathVariable("message") String normalMessage) {
        kafkaTemplate.send("topic_test", normalMessage);
    }
}