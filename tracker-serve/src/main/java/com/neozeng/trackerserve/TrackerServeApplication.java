package com.neozeng.trackerserve;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TrackerServeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrackerServeApplication.class, args);
    }

}
