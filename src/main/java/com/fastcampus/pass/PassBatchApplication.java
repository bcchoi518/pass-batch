package com.fastcampus.pass;

// Spring Batch와 Spring Boot 관련 라이브러리 import
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.batch.core.JobBuilder;
import org.springframework.batch.core.StepBuilder;

// @SpringBootApplication 어노테이션은 이 클래스가 Spring Boot 애플리케이션의 시작점임을 나타냄
@SpringBootApplication
public class PassBatchApplication {

    // 생성자에서 JobRepository, PlatformTransactionManager를 주입받음
    public PassBatchApplication() {}

    @Bean
    public Step passStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("passStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    System.out.println("Execute PassStep");
                    return RepeatStatus.FINISHED;
                }, transactionManager)
                .build();
    }

    @Bean
    public Job passJob(JobRepository jobRepository, Step passStep) {
        return new JobBuilder("passJob", jobRepository)
                .start(passStep)
                .build();
    }

    // main 메서드는 자바 애플리케이션의 진입점
    // SpringApplication.run을 통해 Spring Boot 애플리케이션을 실행
    public static void main(String[] args) {
        SpringApplication.run(PassBatchApplication.class, args);
    }

}
