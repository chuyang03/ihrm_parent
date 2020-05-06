package com.ihrm.system;


import com.ihrm.common.utils.IdWorker;
import com.ihrm.common.utils.JwtUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;

//配置springboot的包扫描
@SpringBootApplication(scanBasePackages = "com.ihrm")
//配置jpa注解的包扫描
@EntityScan(value = "com.ihrm.domain.system")
@EnableEurekaClient

//服务调用，谁调用就在那个启动类上面配置
@EnableDiscoveryClient
@EnableFeignClients
public class SystemApplication {

    /**
     * 启动方法
     *
     */
    public static void main(String[] args) {

        SpringApplication.run(SystemApplication.class, args);
    }


    //雪花算法
    @Bean
    public IdWorker idWorker(){

        return new IdWorker();
    }

    //将其他工程的类引入到当前工程中,生成token的类
    @Bean
    public JwtUtils jwtUtils(){

        return new JwtUtils();
    }

    //解决no session
    @Bean
    public OpenEntityManagerInViewFilter openEntityManagerInViewFilter() {

        return new OpenEntityManagerInViewFilter();
    }

}
