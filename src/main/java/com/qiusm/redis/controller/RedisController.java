package com.qiusm.redis.controller;

import com.alibaba.fastjson.JSONObject;
import com.qiusm.redis.dto.OrgAccount;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * @author qiushengming
 */
@RestController
@RequestMapping("/test/redis")
public class RedisController {

    @Resource(name = "fastjsonRedisTemplate")
    private RedisTemplate<String, Object> fastjsonRedisTemplate;

    @Resource(name = "jackson2JsonRedisTemplate")
    private RedisTemplate<String, Object> jackson2JsonRedisTemplate;

    @GetMapping("fastjsonRedis")
    public Object fastjsonRedis(String key) {
        return fastjsonRedisTemplate.opsForValue().get(key);
    }

    @GetMapping("jackson2JsonRedis")
    public Object jackson2JsonRedis(String key) {
        return jackson2JsonRedisTemplate.opsForValue().get(key);
    }

   private static final String s = "{\"orgCode\":\"testcode123\"," +
           "\"orgAccount\":\"0x2b9ce65679f85ca6ff234109d350f96361f967ef\"," +
            "\"chainPublicKey" +
            "\":\"ebb19a8f1a43825b2f43de39a20fb1dcf61770776638d141c5898089e50cc27267ac1b07830076eff09b8968397581952a364ed7170401767dd4ebf14ba525d4\",\"dataPublicKey\":\"MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCsIX3bVHdP1hUE9cbAV3Uc69SiOSgazHHXCKGF2PZFFwJcOgNTWP8Vogrn5m8qJZ0RWeC6QkAKeLww7AOA+uUZxmihOrsRlWMiKgFQ7eiQeY2XfsGKJwewazSRn6yhpAby6Jr8ZArYpjrUzHo11boBwfJoeRsvzCf8+0d3fKpTUQIDAQAB\",\"orgName\":\"testname123\",\"orgType\":\"99\",\"province\":\"上海\",\"city\":\"上海\",\"ipfs\":null,\"createTime\":\"2022-05-16 14:34:53:098\",\"updateTime\":\"2022-05-16 14:34:53:098\",\"remarks\":\"test-temarks1\"}";

    @GetMapping("fastjsonToRedis")
    public Object fastjsonToRedis() {
        OrgAccount orgAccount = JSONObject.parseObject(s, OrgAccount.class);
        fastjsonRedisTemplate.opsForValue().set("test:fastjson", orgAccount);
        return 1;
    }

    @GetMapping("jackson2jsonToRedis")
    public Object jackson2jsonToRedis() {
        OrgAccount orgAccount = JSONObject.parseObject(s, OrgAccount.class);
        jackson2JsonRedisTemplate.opsForValue().set("test:jackson2json", orgAccount);
        return 1;
    }

    @GetMapping("getString")
    public Object string() {
        return "string";
    }
}
