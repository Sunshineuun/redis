package com.qiusm.redis.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * 1. 交换机和队列关系的绑定关系建立<br>
 * 1.1. 声明交换机的模式,模式类型如下：{@link FanoutExchange},{@link org.springframework.amqp.core.DirectExchange}, {@link org.springframework.amqp.core.TopicExchange}, {@link org.springframework.amqp.core.HeadersExchange}<br>
 * 1.2. 声明队列，{@link Queue}<br>
 * 1.3. 绑定交换机和队列，{@link Binding},{@link BindingBuilder}<br>
 * 2. 监听器构造工厂配置<br>
 *
 * @author qiushengming
 */
@Slf4j
@Configuration
public class RabbitConfig {
    public static final String testQueue = "testQueue";
    public static final String testDirectExchange = "testDirectExchange";
    public static final String testRoutingKey = "testRoutingKey";

//    @Bean
//    public String cusSimpleRabbitListenerContainerFactory(SimpleRabbitListenerContainerFactory containerFactory) {
//        // 监听器批处理设置
//        containerFactory.setBatchListener(true);
//        return "cusSimpleRabbitListenerContainerFactory";
//    }

    /**
     * 队列定义
     */
    @Bean
    public Queue queue() {
        return new Queue(testQueue, true);
    }


    // @Bean("directTtlQueue")
    public Queue directQueue() {
        Map<String, Object> args = new HashMap<>();
        // 值必须是int类型
        args.put("x-message-ttl", 5000);
        // 死信队列配置
        args.put("x-dead-letter-exchange", "direct_dead_order_exchange");
        args.put("x-dead-letter-routing-key", "");
        // 设置信息最大长度,值必须是int类型
        args.put("x-max-length", 1000 );
        return new Queue("direct_ttl_queue_test1", true, false, false, args);
    }

    /**
     * 交换机定义
     */
    @Bean
    DirectExchange directExchange() {
        return new DirectExchange(testDirectExchange, true, false);
    }

    /**
     * 交换机、队列、路由绑定
     */
    @Bean
    Binding tmallBindingDirect() {
        return BindingBuilder.bind(queue()).to(directExchange()).with(testRoutingKey);
    }
}
