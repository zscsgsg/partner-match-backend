package com.zsc.partnermatch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * WebSocket 配置类
 * 注册 WebSocket 端点，使其可以被客户端连接
 */
@Configuration
public class WebSocketConfig {

    /**
     * 注册 WebSocket 端点
     * ServerEndpointExporter 会自动注册 @ServerEndpoint 注解的类
     */
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
}
