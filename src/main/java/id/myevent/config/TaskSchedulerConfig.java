package id.myevent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Task Scheduler Config.
 */
@Configuration
@ComponentScan(
    basePackages = "id.myevent.config",
    basePackageClasses = {
        TaskSchedulerConfig.class,
    })
public class TaskSchedulerConfig {
  @Bean
  public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
    ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
    threadPoolTaskScheduler.setPoolSize(20);
    threadPoolTaskScheduler.setThreadNamePrefix("ThreadPoolTaskScheduler");
    threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
    return threadPoolTaskScheduler;
  }
}
