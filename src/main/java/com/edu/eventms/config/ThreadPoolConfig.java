package com.edu.eventms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ThreadPoolConfig {
    private static final int FIXED_POOL_SIZE = 10;

    @Bean
    public ExecutorService eventExecutorService() {
        return Executors.newFixedThreadPool(FIXED_POOL_SIZE);
    }
}
