package com.crisp.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 测试启动类 — 不启用 Redis Session / Cache
 */
@SpringBootApplication
public class TestDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(TestDemoApplication.class, args);
    }
}
