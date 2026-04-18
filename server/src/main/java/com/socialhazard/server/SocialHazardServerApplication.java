package com.socialhazard.server;

import com.socialhazard.server.config.WebSocketConfig;
import com.socialhazard.server.websocket.GameWebSocketHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackageClasses = {
        SocialHazardServerApplication.class,
        WebSocketConfig.class,
        GameWebSocketHandler.class
})
@EnableScheduling
@ConfigurationPropertiesScan
public class SocialHazardServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialHazardServerApplication.class, args);
    }
}
