package com.ecoinsight.bdsp.asd;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
     private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    // @Bean(name="asyncExecutor")
    

    
    @Override
    @Bean(name = "asyncExecutor")
    public Executor getAsyncExecutor() {
          ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8); // 기본적으로 실행 대기 중인 Thread 개수
        executor.setMaxPoolSize(10); // 동시에 동작하는 최대 Thread 개수
        executor.setQueueCapacity(100); // CorePool이 초과될때 Queue에 저장했다가 꺼내서 실행된다. (500개까지 저장함)
        // 단, MaxPoolSize가 초과되면 Thread 생성에 실패할 수 있음.
        // 참고: https://medium.com/trendyol-tech/spring-boot-async-executor-management-with-threadpooltaskexecutor-f493903617d
        // jooncco님 수정사항
        executor.setThreadNamePrefix("async-"); // Spring에서 생성하는 Thread 이름의 접두사
        executor.initialize();
        return executor;   
    }
    // public Executor getAsynExecutor() {
    //   ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    //     executor.setCorePoolSize(2); // 기본적으로 실행 대기 중인 Thread 개수
    //     executor.setMaxPoolSize(10); // 동시에 동작하는 최대 Thread 개수
    //     executor.setQueueCapacity(500); // CorePool이 초과될때 Queue에 저장했다가 꺼내서 실행된다. (500개까지 저장함)
    //     // 단, MaxPoolSize가 초과되면 Thread 생성에 실패할 수 있음.
    //     // 참고: https://medium.com/trendyol-tech/spring-boot-async-executor-management-with-threadpooltaskexecutor-f493903617d
    //     // jooncco님 수정사항
    //     executor.setThreadNamePrefix("async-"); // Spring에서 생성하는 Thread 이름의 접두사
    //     executor.initialize();
    //     return executor;   
    // }



        @Override
        public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
                   // TODO Auto-generated method stub
        return (ex, method, params) ->
         LOGGER.error("Exception handler for async method '" + method.toGenericString()
           + "' threw unexpected exception itself", ex);
        }


}
