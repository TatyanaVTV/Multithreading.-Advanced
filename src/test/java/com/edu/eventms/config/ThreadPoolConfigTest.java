package com.edu.eventms.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.ExecutorService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ThreadPoolConfigTest {

    @Autowired
    private ExecutorService executorService;

    @Test
    void shouldCreateExecutorServiceBean() {
        assertThat(executorService).isNotNull();
        assertThat(executorService.isShutdown()).isFalse();
        assertThat(executorService.isTerminated()).isFalse();
    }
}
