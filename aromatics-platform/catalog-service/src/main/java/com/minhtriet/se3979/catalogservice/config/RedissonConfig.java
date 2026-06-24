package com.minhtriet.se3979.catalogservice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        // Cấu hình kết nối động đọc từ application.yml
        String redisAddress = String.format("redis://%s:%s", redisHost, redisPort);

        config.useSingleServer()
                .setAddress(redisAddress)
                .setConnectionMinimumIdleSize(5)
                .setConnectionPoolSize(50); // Tối ưu connection pool cho hiệu suất cao

        return Redisson.create(config);
    }
}