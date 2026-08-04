package com.shaanxi.zhiping;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 陕西综评单招刷题小程序后端启动类
 */
@SpringBootApplication
@MapperScan("com.shaanxi.zhiping.mapper")
public class ZhiPingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZhiPingApplication.class, args);
    }
}
