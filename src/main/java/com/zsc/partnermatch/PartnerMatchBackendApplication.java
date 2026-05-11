package com.zsc.partnermatch;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.zsc.partnermatch.mapper")
@EnableScheduling  // 开启定时任务
@EnableAsync       // 开启异步支持（事件监听器 @Async 需要）
public class PartnerMatchBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerMatchBackendApplication.class, args);
    }

}
