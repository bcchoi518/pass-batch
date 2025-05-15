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

// ExpirePassesJobConfig 클래스는 만료 대상 이용권을 찾아 상태를 변경하는 Spring Batch Job/Step을 설정하는 클래스입니다.
@Configuration // 이 클래스가 Spring 설정 파일임을 명시
public class ExpirePassesJobConfig {
    // 한 번에 처리할 데이터 개수(청크 사이즈) 지정
    private final int CHUNK_SIZE = 5; // 청크 단위(한 번에 처리할 데이터 개수)

    // EntityManagerFactory: JPA의 EntityManager를 생성하는 팩토리, DB 접근에 사용
    private final EntityManagerFactory entityManagerFactory;

    // 생성자에서 EntityManagerFactory만 주입받음
    public ExpirePassesJobConfig(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    // 만료 Step을 생성하는 메서드
    // JobRepository: 배치 잡의 메타데이터를 관리하는 객체
    // PlatformTransactionManager: 트랜잭션 처리를 담당하는 객체
    @Bean
    public Step expirePassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("expirePassesStep", jobRepository)
                .<PassEntity, PassEntity>chunk(CHUNK_SIZE, transactionManager) // 청크 단위로 처리
                .reader(expirePassesItemReader()) // 만료 대상 조회
                .processor(expirePassesItemProcessor()) // 상태 변경 처리
                .writer(expirePassesItemWriter()) // DB에 반영
                .build();
    }

    // 만료 Job을 생성하는 메서드
    // JobRepository: 배치 잡의 메타데이터를 관리하는 객체
    // Step: 만료 Step
    @Bean
    public Job expirePassesJob(JobRepository jobRepository, Step expirePassesStep) {
        return new JobBuilder("expirePassesJob", jobRepository)
                .start(expirePassesStep) // 첫 Step으로 expirePassesStep 지정
                .build();
    }

    /**
     * JpaCursorItemReader: JPA에서 커서 기반으로 데이터를 읽어오는 리더
     * 페이징 기법보다 높은 성능, 데이터 변경에 무관한 무결성 조회 가능
     * 만료 대상(상태: PROGRESSED, 종료일시: 현재 이전)만 조회
     */
    @Bean
    public JpaCursorItemReader<PassEntity> expirePassesItemReader() {
        return new JpaCursorItemReaderBuilder<PassEntity>()
                .name("expirePassesItemReader") // 리더 이름 지정
                .entityManagerFactory(entityManagerFactory) // JPA EntityManagerFactory 설정
                .queryString("select p from PassEntity p where p.status = :status and p.endedAt <= :endedAt") // JPQL 쿼리
                .parameterValues(Map.of("status", PassStatus.PROGRESSED, "endedAt", LocalDateTime.now())) // 파라미터 바인딩
                .build();
    }

    // 만료 처리 ItemProcessor: 상태를 EXPIRED로 변경하고 만료일시를 기록
    @Bean
    public ItemProcessor<PassEntity, PassEntity> expirePassesItemProcessor() {
        return passEntity -> {
            passEntity.setStatus(PassStatus.EXPIRED); // 상태를 만료(EXPIRED)로 변경
            passEntity.setExpiredAt(LocalDateTime.now()); // 만료일시를 현재로 기록
            return passEntity;
        };
    }

    /**
     * JpaItemWriter: JPA의 영속성 관리를 위해 EntityManager를 필수로 설정
     * 만료 처리된 이용권을 DB에 저장
     */
    @Bean
    public JpaItemWriter<PassEntity> expirePassesItemWriter() {
        return new JpaItemWriterBuilder<PassEntity>()
                .entityManagerFactory(entityManagerFactory) // JPA EntityManagerFactory 설정
                .build();
    }

}
