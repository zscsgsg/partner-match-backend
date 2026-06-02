package com.zsc.partnermatch.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.data.redis")
@Data
public class RedissonConfig {

    private String host;
    private int  port;
    private String password;



    @Bean
    public RedissonClient redissonClient() {
        // 1. 创建配置
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        // 2. 配置单节点连接方式 (为你的部署结构选择正确的配置)
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password)
                .setDatabase(3);

        // 3. 创建并返回客户端实例
        return Redisson.create(config);
    }
}