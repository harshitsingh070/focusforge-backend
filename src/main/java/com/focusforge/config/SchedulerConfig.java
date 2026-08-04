package com.focusforge.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@EnableScheduling
public class SchedulerConfig {

    // A small, dedicated scheduler pool keeps this keep-alive ping (and any other
    // @Scheduled jobs) off the request-handling threads and off each other. Without
    // this, Spring runs every @Scheduled task on a single shared thread, so a slow
    // or blocking ping would delay all other scheduled work. (The pool also backs
    // the existing @EnableScheduling on the main application class.)
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(5);
        scheduler.setThreadNamePrefix("scheduled-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setErrorHandler(t ->
                log.error("Uncaught exception in scheduled task", t));
        return scheduler;
    }

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(SchedulerConfig.class);
}
