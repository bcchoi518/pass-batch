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

// 이 클래스가 Spring 설정 파일임을 나타내는 어노테이션
@Configuration // Spring 설정 클래스임을 명시
public class UsePassesJobConfig {
    // 한 번에 처리할 데이터 개수(청크 사이즈) 지정
    private final int CHUNK_SIZE = 10; // 청크 단위(한 번에 처리할 데이터 개수)

    // Spring Batch에서 제공하는 Job/Step 빌더 팩토리 및 의존성 주입
    private final EntityManagerFactory entityManagerFactory;
    private final PassRepository passRepository;
    private final BookingRepository bookingRepository;

    // 생성자에서 EntityManagerFactory, PassRepository, BookingRepository만 주입받음
    public UsePassesJobConfig(EntityManagerFactory entityManagerFactory, PassRepository passRepository, BookingRepository bookingRepository) {
        this.entityManagerFactory = entityManagerFactory;
        this.passRepository = passRepository;
        this.bookingRepository = bookingRepository;
    }

    @Bean
    public Step usePassesStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("usePassesStep", jobRepository)
                .<BookingEntity, Future<BookingEntity>>chunk(CHUNK_SIZE, transactionManager)
                .reader(usePassesItemReader())
                .processor(usePassesAsyncItemProcessor())
                .writer(usePassesAsyncItemWriter())
                .build();
    }

    @Bean
    public Job usePassesJob(JobRepository jobRepository, Step usePassesStep) {
        return new JobBuilder("usePassesJob", jobRepository)
                .start(usePassesStep)
                .build();
    }

    // 이용권 사용 대상 예약을 읽어오는 JPA Cursor ItemReader
    @Bean
    public JpaCursorItemReader<BookingEntity> usePassesItemReader() {
        // JPA Cursor 방식으로 예약 데이터를 읽어옴
        // 예약 상태가 COMPLETED이고, 이용권 미사용이며, 종료일시가 현재보다 이전인 예약만 조회
        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("usePassesItemReader")
                .entityManagerFactory(entityManagerFactory)
                .queryString("select b from BookingEntity b join fetch b.passEntity where b.status = :status and b.usedPass = false and b.endedAt < :endedAt")
                .parameterValues(Map.of("status", BookingStatus.COMPLETED, "endedAt", LocalDateTime.now()))
                .build();
    }

    // 비동기 ItemProcessor 설정
    @Bean
    public AsyncItemProcessor<BookingEntity, BookingEntity> usePassesAsyncItemProcessor() {
        // 실제 처리 로직을 위임할 ItemProcessor와 비동기 실행을 위한 TaskExecutor 설정
        AsyncItemProcessor<BookingEntity, BookingEntity> asyncItemProcessor = new AsyncItemProcessor<>();
        asyncItemProcessor.setDelegate(usePassesItemProcessor()); // 실제 처리 로직 위임
        asyncItemProcessor.setTaskExecutor(new SimpleAsyncTaskExecutor()); // 비동기 실행
        return asyncItemProcessor;
    }

    // 이용권 사용 처리 ItemProcessor: 남은 횟수 차감, 사용 처리
    @Bean
    public ItemProcessor<BookingEntity, BookingEntity> usePassesItemProcessor() {
        // 예약 엔티티에서 이용권 정보를 꺼내 남은 횟수를 1 차감하고, 사용 처리
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
        // 실제 저장 로직을 위임할 ItemWriter 설정
        AsyncItemWriter<BookingEntity> asyncItemWriter = new AsyncItemWriter<>();
        asyncItemWriter.setDelegate(usePassesItemWriter()); // 실제 저장 로직 위임
        return asyncItemWriter;
    }

    // 이용권 사용 처리 결과를 DB에 반영하는 ItemWriter
    @Bean
    public ItemWriter<BookingEntity> usePassesItemWriter() {
        // 예약 엔티티 리스트를 받아 DB에 남은 횟수, 사용 여부를 업데이트
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
