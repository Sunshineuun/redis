package com.qiusm.redis.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaInitialConfiguration {
    /**
     * 创建一个名为testtopic的Topic并设置分区数为8，分区副本数为2
     */
    @Bean
    public NewTopic initialTopic() {
        return new NewTopic("test_topic", 8, (short) 1);
    }

    /**
     * 如果要修改分区数，只需修改配置值重启项目即可 <br>
     * 修改分区数并不会导致数据的丢失，但是分区数只能增大不能减小
     */
    @Bean
    public NewTopic updateTopic() {
        return new NewTopic("test_topic1", 10, (short) 1);
    }
}