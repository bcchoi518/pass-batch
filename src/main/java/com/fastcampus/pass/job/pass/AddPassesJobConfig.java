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

// AddPassesJobConfig 클래스는 Spring Batch의 Job과 Step을 설정하는 클래스입니다.
// @Configuration 어노테이션을 통해 스프링 설정 클래스로 등록됩니다.
@Configuration
public class AddPassesJobConfig {
    // 기본 생성자입니다. 별도의 의존성 주입 없이 사용됩니다.
    public AddPassesJobConfig() {}

    // addPassesStep 메서드는 AddPassesTasklet을 실행하는 Step을 생성합니다.
    // @Bean 어노테이션으로 스프링 빈으로 등록됩니다.
    // JobRepository: 배치 잡의 메타데이터를 관리하는 객체
    // PlatformTransactionManager: 트랜잭션 처리를 담당하는 객체
    // AddPassesTasklet: 실제 비즈니스 로직을 수행하는 Tasklet
    @Bean
    public Step addPassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, AddPassesTasklet addPassesTasklet) {
        // StepBuilder를 사용해 Step을 생성하고, Tasklet을 등록합니다.
        return new StepBuilder("addPassesStep", jobRepository)
                .tasklet(addPassesTasklet, transactionManager) // Tasklet과 트랜잭션 매니저를 설정
                .build(); // Step 생성
    }

    // addPassesJob 메서드는 addPassesStep을 실행하는 Job을 생성합니다.
    // @Bean 어노테이션으로 스프링 빈으로 등록됩니다.
    // JobRepository: 배치 잡의 메타데이터를 관리하는 객체
    // Step: 배치 잡에서 실행할 Step
    @Bean
    public Job addPassesJob(JobRepository jobRepository, Step addPassesStep) {
        // JobBuilder를 사용해 Job을 생성하고, Step을 등록합니다.
        return new JobBuilder("addPassesJob", jobRepository)
                .start(addPassesStep) // 첫 번째 Step으로 addPassesStep을 설정
                .build(); // Job 생성
    }

}
