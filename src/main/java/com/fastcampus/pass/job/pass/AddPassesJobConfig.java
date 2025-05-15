package com.fastcampus.pass.job.pass;

// Spring Batch 관련 import
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.JobBuilder;
import org.springframework.batch.core.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// 이 클래스가 Spring 설정 파일임을 나타내는 어노테이션
@Configuration
public class AddPassesJobConfig {
    // 생성자에서 JobRepository, PlatformTransactionManager, AddPassesTasklet을 주입받음
    public AddPassesJobConfig() {}

    @Bean
    public Step addPassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, AddPassesTasklet addPassesTasklet) {
        return new StepBuilder("addPassesStep", jobRepository)
                .tasklet(addPassesTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job addPassesJob(JobRepository jobRepository, Step addPassesStep) {
        return new JobBuilder("addPassesJob", jobRepository)
                .start(addPassesStep)
                .build();
    }

}
