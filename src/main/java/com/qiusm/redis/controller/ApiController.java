package com.qiusm.redis.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * @author qiushengming
 */
@RequestMapping("/api")
@RestController
public class ApiController {

    @GetMapping("getList")
    public List<String> getList() {
        return Arrays.asList("a,b,c,d".split(","));
    }
}
