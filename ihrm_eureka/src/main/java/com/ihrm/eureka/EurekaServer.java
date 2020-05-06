package com.ihrm.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 *
 * eureka服务端启动类
 */
@SpringBootApplication
@EnableEurekaServer   //表示这是一个eureka服务端
public class EurekaServer {

    public static void main(String[] args) {

        SpringApplication.run(EurekaServer.class);
    }
}
