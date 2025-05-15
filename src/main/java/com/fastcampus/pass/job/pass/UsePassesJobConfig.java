package com.fastcampus.pass.job.pass;

// BookingEntity: 예약 정보를 담는 엔티티 클래스 import
import com.fastcampus.pass.repository.booking.BookingEntity;
// BookingRepository: 예약 정보에 접근하는 JPA 레포지토리 import
import com.fastcampus.pass.repository.booking.BookingRepository;
// BookingStatus: 예약 상태를 나타내는 enum import
import com.fastcampus.pass.repository.booking.BookingStatus;
// PassEntity: 이용권 정보를 담는 엔티티 클래스 import
import com.fastcampus.pass.repository.pass.PassEntity;
// PassRepository: 이용권 정보에 접근하는 JPA 레포지토리 import
import com.fastcampus.pass.repository.pass.PassRepository;
// Spring Batch 관련 import
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.JobBuilder;
import org.springframework.batch.core.StepBuilder;
import org.springframework.batch.integration.async.AsyncItemProcessor;
import org.springframework.batch.integration.async.AsyncItemWriter;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

// jakarta.persistence로 네임스페이스 변경
import jakarta.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.Future;

// UsePassesJobConfig 클래스는 예약 완료된 건에 대해 이용권 사용 처리를 수행하는 Spring Batch Job/Step을 설정하는 클래스입니다.
@Configuration // Spring 설정 클래스임을 명시
public class UsePassesJobConfig {
    // 한 번에 처리할 데이터 개수(청크 사이즈) 지정
    private final int CHUNK_SIZE = 10; // 청크 단위(한 번에 처리할 데이터 개수)

    // EntityManagerFactory: JPA의 EntityManager를 생성하는 팩토리, DB 접근에 사용
    private final EntityManagerFactory entityManagerFactory;
    // PassRepository: 이용권 정보에 접근하는 JPA 레포지토리
    private final PassRepository passRepository;
    // BookingRepository: 예약 정보에 접근하는 JPA 레포지토리
    private final BookingRepository bookingRepository;

    // 생성자에서 EntityManagerFactory, PassRepository, BookingRepository만 주입받음
    public UsePassesJobConfig(EntityManagerFactory entityManagerFactory, PassRepository passRepository, BookingRepository bookingRepository) {
        this.entityManagerFactory = entityManagerFactory;
        this.passRepository = passRepository;
        this.bookingRepository = bookingRepository;
    }

    // 이용권 사용 Step을 생성하는 메서드
    // JobRepository: 배치 잡의 메타데이터를 관리하는 객체
    // PlatformTransactionManager: 트랜잭션 처리를 담당하는 객체
    @Bean
    public Step usePassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("usePassesStep", jobRepository)
                .<BookingEntity, Future<BookingEntity>>chunk(CHUNK_SIZE, transactionManager) // 비동기 처리를 위해 Future 타입 사용
                .reader(usePassesItemReader()) // 예약 데이터 조회
                .processor(usePassesAsyncItemProcessor()) // 비동기 처리
                .writer(usePassesAsyncItemWriter()) // 비동기 저장
                .build();
    }

    // 이용권 사용 Job을 생성하는 메서드
    // JobRepository: 배치 잡의 메타데이터를 관리하는 객체
    // Step: 이용권 사용 Step
    @Bean
    public Job usePassesJob(JobRepository jobRepository, Step usePassesStep) {
        return new JobBuilder("usePassesJob", jobRepository)
                .start(usePassesStep) // 첫 Step으로 usePassesStep 지정
                .build();
    }

    // 이용권 사용 대상 예약을 읽어오는 JPA Cursor ItemReader
    @Bean
    public JpaCursorItemReader<BookingEntity> usePassesItemReader() {
        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("usePassesItemReader") // 리더 이름 지정
                .entityManagerFactory(entityManagerFactory) // JPA EntityManagerFactory 설정
                .queryString("select b from BookingEntity b join fetch b.passEntity where b.status = :status and b.usedPass = false and b.endedAt < :endedAt") // JPQL 쿼리
                .parameterValues(Map.of("status", BookingStatus.COMPLETED, "endedAt", LocalDateTime.now())) // 파라미터 바인딩
                .build();
    }

    // 비동기 ItemProcessor 설정
    @Bean
    public AsyncItemProcessor<BookingEntity, BookingEntity> usePassesAsyncItemProcessor() {
        AsyncItemProcessor<BookingEntity, BookingEntity> asyncItemProcessor = new AsyncItemProcessor<>(); // 비동기 ItemProcessor 생성
        asyncItemProcessor.setDelegate(usePassesItemProcessor()); // 실제 처리 로직 위임
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor()); // 비동기 실행을 위한 TaskExecutor 설정
        return asyncItemProcessor;
    }

    // 이용권 사용 처리 ItemProcessor: 남은 횟수 차감, 사용 처리
    @Bean
    public ItemProcessor<BookingEntity, BookingEntity> usePassesItemProcessor() {
        return bookingEntity -> {
            PassEntity passEntity = bookingEntity.getPassEntity(); // 예약에 연결된 이용권 정보 조회
            passEntity.setRemainingCount(passEntity.getRemainingCount() - 1); // 남은 횟수 차감
            bookingEntity.setPassEntity(passEntity); // 변경된 이용권 정보 저장

            bookingEntity.setUsedPass(true); // 사용 처리
            return bookingEntity;
        };
    }

    // 비동기 ItemWriter 설정
    @Bean
    public AsyncItemWriter<BookingEntity> usePassesAsyncItemWriter() {
        AsyncItemWriter<BookingEntity> asyncItemWriter = new AsyncItemWriter<>(); // 비동기 ItemWriter 생성
        asyncItemWriter.setDelegate(usePassesItemWriter()); // 실제 저장 로직 위임
        return asyncItemWriter;
    }

    // 이용권 사용 처리 결과를 DB에 반영하는 ItemWriter
    @Bean
    public ItemWriter<BookingEntity> usePassesItemWriter() {
        return bookingEntities -> {
            for(BookingEntity bookingEntity: bookingEntities) {
                // 이용권 남은 횟수 업데이트
                int updatedCount = passRepository.updateRemainingCount(bookingEntity.getPassSeq(), bookingEntity.getPassEntity().getRemainingCount());

                // 남은 횟수 업데이트가 성공하면 사용 여부도 업데이트
                if (updatedCount > 0) {
                    bookingRepository.updateUsedPass(bookingEntity.getPassSeq(), bookingEntity.isUsedPass());
                }
            }
        };
    }
}
