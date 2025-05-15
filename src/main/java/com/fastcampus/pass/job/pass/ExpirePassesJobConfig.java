package com.fastcampus.pass.job.pass;

// PassEntity: 이용권 정보를 담는 엔티티 클래스 import
import com.fastcampus.pass.repository.pass.PassEntity;
// PassStatus: 이용권 상태를 나타내는 enum import
import com.fastcampus.pass.repository.pass.PassStatus;
// Spring Batch 관련 import
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.JobBuilder;
import org.springframework.batch.core.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// jakarta.persistence로 네임스페이스 변경
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;

// 이 클래스가 Spring 설정 파일임을 나타내는 어노테이션
@Configuration // Spring 설정 클래스임을 명시
public class ExpirePassesJobConfig {
    // 한 번에 처리할 데이터 개수(청크 사이즈) 지정
    private final int CHUNK_SIZE = 5; // 청크 단위(한 번에 처리할 데이터 개수)

    // Spring Batch에서 제공하는 Job/Step 빌더 팩토리 및 의존성 주입
    private final EntityManagerFactory entityManagerFactory;

    // 생성자에서 EntityManagerFactory만 주입받음
    public ExpirePassesJobConfig(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Bean
    public Step expirePassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("expirePassesStep", jobRepository)
                .<PassEntity, PassEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(expirePassesItemReader())
                .processor(expirePassesItemProcessor())
                .writer(expirePassesItemWriter())
                .build();
    }

    @Bean
    public Job expirePassesJob(JobRepository jobRepository, Step expirePassesStep) {
        return new JobBuilder("expirePassesJob", jobRepository)
                .start(expirePassesStep)
                .build();
    }

    /**
     * JpaCursorItemReader: JPA에서 커서 기반으로 데이터를 읽어오는 리더
     * 페이징 기법보다 높은 성능, 데이터 변경에 무관한 무결성 조회 가능
     */
    @Bean
    public JpaCursorItemReader<PassEntity> expirePassesItemReader() {
        // JPA Cursor 방식으로 이용권 데이터를 읽어옴
        // 상태가 PROGRESSED이고, 종료일시가 현재보다 이전인 이용권만 조회
        return new JpaCursorItemReaderBuilder<PassEntity>()
                .name("expirePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select p from PassEntity p where p.status = :status and p.endedAt <= :endedAt")
                .parameterValues(Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now()))
                .build();
    }

    // 만료 처리 ItemProcessor: 상태를 EXPIRED로 변경하고 만료일시를 기록
    @Bean
    public ItemProcessor<PassEntity, PassEntity> expirePassesItemProcessor() {
        // 이용권 엔티티의 상태를 만료(EXPIRED)로 변경하고, 만료일시를 현재로 기록
        return passEntity -> {
            passEntity.setStatus(PassStatus.EXPIRED); // 상태를 만료로 변경
            passEntity.setExpiredAt(LocalDateTime.now()); // 만료일시 기록
            return passEntity;
        };
    }

    /**
     * JpaItemWriter: JPA의 영속성 관리를 위해 EntityManager를 필수로 설정
     */
    @Bean
    public JpaItemWriter<PassEntity> expirePassesItemWriter() {
        // JPA를 통해 만료된 이용권 정보를 DB에 저장
        return new JpaItemWriterBuilder<PassEntity>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }

}
