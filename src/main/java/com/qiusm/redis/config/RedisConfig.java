package com.qiusm.redis.config;

import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.config.FastJsonConfig;
import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Jackson2Json 相对 FastJson 稍微慢一点
 * @author qiushengming
 */
@Configuration
@Slf4j
//@ConditionalOnBean(value = RedisConnectionFactory.class)
public class RedisConfig {
    /**
     * redis key value 序列化配置
     *
     * @param redisConnectionFactory 链接创建工厂
     * @return
     */
    @Bean
//    @ConditionalOnBean(value = RedisConnectionFactory.class)
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        log.info("开始构建 RedisTemplate");
        RedisTemplate<Object, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //配置序列化规则,jdk的序列化，对象必须实现Serializer接口jackson就不需要
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        //实例化
        ObjectMapper objectMapper = new ObjectMapper();
        //全部属性都能实例化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jackson2JsonRedisSerializer.setObjectMapper(objectMapper);


        //设置key-value序列化规则
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);

        //设置hash-value序列化规则
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);

        return redisTemplate;
    }

    /**
     * key 已 string 的方式序列化，value 用 fastjson 进行序列化。类型自动转换。<br>
     */
    @Bean
//    @ConditionalOnBean(value = RedisConnectionFactory.class)
    public RedisTemplate<String, Object> strRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 配置连接工厂
        template.setConnectionFactory(redisConnectionFactory);
        // 配置 redis 与 spring 交互时可以达到自动转化为javabean。如果不配置则转化为JSONObject
        ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        // 白名单设置
        ParserConfig.getGlobalInstance().addAccept("com.simmed.bcos2.dto.response.");
        ParserConfig.getGlobalInstance().addAccept("com.simmed.bcos2.dto.org.");
        FastJsonConfig fastJsonConfig = new FastJsonConfig();
        fastJsonConfig.setSerializerFeatures(SerializerFeature.WriteClassName);

        // fastjson 序列化
        FastJsonRedisSerializer<?> fastJsonSerial = new FastJsonRedisSerializer<>(Object.class);
        fastJsonSerial.setFastJsonConfig(fastJsonConfig);
        // String 序列化
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // key的序列化方式指定
        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);

        // value的序列化方式指定
        template.setValueSerializer(fastJsonSerial);
        template.setHashValueSerializer(fastJsonSerial);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 对hash类型的数据操作
     */
    @Bean
    public HashOperations<String, String, Object> hashOperations(
            @Qualifier(value = "strRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForHash();
    }

    /**
     * 对redis字符串类型数据操作
     */
    @Bean
    public ValueOperations<String, Object> valueOperations(
            @Qualifier(value = "strRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    /**
     * 对链表类型的数据操作
     */
    @Bean
    public ListOperations<String, Object> listOperations(
            @Qualifier(value = "strRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForList();
    }

    /**
     * 对无序集合类型的数据操作
     */
    @Bean
    public SetOperations<String, Object> setOperations(
            @Qualifier(value = "strRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    /**
     * 对有序集合类型的数据操作
     */
    @Bean
    public ZSetOperations<String, Object> zSetOperations(
            @Qualifier(value = "strRedisTemplate") RedisTemplate<String, Object> redisTemplate) {
        return redisTemplate.opsForZSet();
    }
}
