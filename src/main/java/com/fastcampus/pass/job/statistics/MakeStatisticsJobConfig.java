package com.fastcampus.pass.job.statistics;

// 예약(Booking) 엔티티를 사용하기 위해 import 합니다.
import com.fastcampus.pass.repository.booking.BookingEntity;
// 통계(Statistics) 엔티티와 레포지토리를 사용하기 위해 import 합니다.
import com.fastcampus.pass.repository.statistics.StatisticsEntity;
import com.fastcampus.pass.repository.statistics.StatisticsRepository;
// 날짜/시간 관련 유틸리티를 사용하기 위해 import 합니다.
import com.fastcampus.pass.util.LocalDateTimeUtils;
// 로그 출력을 위한 Lombok 어노테이션입니다.
import lombok.extern.slf4j.Slf4j;
// Spring Batch의 Job, Step 등 배치 관련 클래스를 import 합니다.
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

// JPA에서 EntityManagerFactory를 사용하기 위해 import 합니다.
import jakarta.persistence.EntityManagerFactory;
// 날짜/시간 처리를 위한 클래스들입니다.
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.batch.core.repository.JobRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.batch.core.JobBuilder;
import org.springframework.batch.core.StepBuilder;

// 로그를 사용할 수 있게 해주는 Lombok 어노테이션입니다.
@Slf4j
// 이 클래스가 스프링 설정 클래스임을 나타냅니다.
@Configuration
public class MakeStatisticsJobConfig {
    // 한 번에 처리할 데이터의 개수를 지정합니다.
    private final int CHUNK_SIZE = 10;

    // EntityManagerFactory: JPA의 EntityManager를 생성하는 팩토리, DB 접근에 사용
    private final EntityManagerFactory entityManagerFactory;
    // StatisticsRepository: 통계 정보를 저장/조회하는 JPA Repository
    private final StatisticsRepository statisticsRepository;
    // MakeDailyStatisticsTasklet: 일간 통계 처리를 위한 Tasklet
    private final MakeDailyStatisticsTasklet makeDailyStatisticsTasklet;
    // MakeWeeklyStatisticsTasklet: 주간 통계 처리를 위한 Tasklet
    private final MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet;

    // 생성자에서 EntityManagerFactory, StatisticsRepository, Tasklet을 주입받음
    public MakeStatisticsJobConfig(EntityManagerFactory entityManagerFactory, StatisticsRepository statisticsRepository, MakeDailyStatisticsTasklet makeDailyStatisticsTasklet, MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet) {
        this.entityManagerFactory = entityManagerFactory; // DB 접근용
        this.statisticsRepository = statisticsRepository; // 통계 저장/조회용
        this.makeDailyStatisticsTasklet = makeDailyStatisticsTasklet; // 일간 통계 Tasklet
        this.makeWeeklyStatisticsTasklet = makeWeeklyStatisticsTasklet; // 주간 통계 Tasklet
    }

    // 예약 데이터를 통계로 변환하는 Step을 생성하는 메서드
    // JobRepository: 배치 잡의 메타데이터를 관리하는 객체
    // PlatformTransactionManager: 트랜잭션 처리를 담당하는 객체
    @Bean
    public Step addStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("addStatisticsStep", jobRepository)
                .<BookingEntity, BookingEntity>chunk(CHUNK_SIZE, transactionManager) // 청크 단위로 처리
                .reader(addStatisticsItemReader(null, null)) // 예약 데이터 조회
                .writer(addStatisticsItemWriter()) // 통계로 변환 및 저장
                .build();
    }

    // 일간 통계 생성 Step을 생성하는 메서드
    @Bean
    public Step makeDailyStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("makeDailyStatisticsStep", jobRepository)
                .tasklet(makeDailyStatisticsTasklet, transactionManager) // 일간 통계 Tasklet 실행
                .build();
    }

    // 주간 통계 생성 Step을 생성하는 메서드
    @Bean
    public Step makeWeeklyStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("makeWeeklyStatisticsStep", jobRepository)
                .tasklet(makeWeeklyStatisticsTasklet, transactionManager) // 주간 통계 Tasklet 실행
                .build();
    }

    // 통계 집계 전체 Job을 생성하는 메서드
    // addStatisticsStep → (일간/주간 통계 Step 병렬 실행)
    @Bean
    public Job makeStatisticsJob(JobRepository jobRepository, Step addStatisticsStep, Step makeDailyStatisticsStep, Step makeWeeklyStatisticsStep) {
        // 예약 데이터 → 통계 변환 Flow
        Flow addStatisticsFlow = new FlowBuilder<Flow>("addStatisticsFlow")
                .start(addStatisticsStep)
                .build();
        // 일간 통계 Flow
        Flow makeDailyStatisticsFlow = new FlowBuilder<Flow>("makeDailyStatisticsFlow")
                .start(makeDailyStatisticsStep)
                .build();
        // 주간 통계 Flow
        Flow makeWeeklyStatisticsFlow = new FlowBuilder<Flow>("makeWeeklyStatisticsFlow")
                .start(makeWeeklyStatisticsStep)
                .build();
        // 일간/주간 통계를 병렬로 실행하는 Flow
        Flow parallelMakeStatisticsFlow = new FlowBuilder<Flow>("parallelMakeStatisticsFlow")
                .split(new SimpleAsyncTaskExecutor()) // 병렬 실행을 위한 TaskExecutor
                .add(makeDailyStatisticsFlow, makeWeeklyStatisticsFlow)
                .build();
        // JobBuilder를 사용해 Job을 생성하고, Flow를 등록
        return new JobBuilder("makeStatisticsJob", jobRepository)
                .start(addStatisticsFlow) // 첫 Flow: 예약→통계 변환
                .next(parallelMakeStatisticsFlow) // 다음: 일간/주간 통계 병렬 실행
                .build()
                .build();
    }

    // Step 실행 시점에 파라미터를 주입받기 위해 @StepScope를 사용합니다.
    // 예약 데이터를 기간(from~to) 내에서 조회하는 JPA Cursor ItemReader를 생성
    @Bean
    @StepScope
    public JpaCursorItemReader<BookingEntity> addStatisticsItemReader(
            @Value("#{jobParameters[from]}") String fromString,
            @Value("#{jobParameters[to]}") String toString
    ) {
        // 문자열로 받은 날짜 파라미터를 LocalDateTime으로 변환
        final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
        final LocalDateTime to = LocalDateTimeUtils.parse(toString);

        // JPA를 이용해 BookingEntity를 읽어오는 ItemReader를 생성
        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("addStatisticsItemReader") // 리더 이름 지정
                .entityManagerFactory(entityManagerFactory) // DB 연결 정보
                .queryString("select b from BookingEntity b where b.endedAt between :from and :to") // 기간 내 예약 정보만 조회
                .parameterValues(Map.of("from", from, "to", to)) // 쿼리 파라미터 바인딩
                .build();
    }

    // 예약 정보를 통계로 변환하여 저장하는 Writer를 등록
    @Bean
    public ItemWriter<BookingEntity> addStatisticsItemWriter() {
        return bookingEntities -> {
            // 날짜별로 통계 정보를 모으기 위한 Map
            Map<LocalDateTime, StatisticsEntity> statisticsEntityMap = new LinkedHashMap<>();

            // 예약 엔티티 리스트를 순회
            for (BookingEntity bookingEntity: bookingEntities) {
                // 예약의 통계 기준 날짜를 가져옴
                final LocalDateTime statisticsAt = bookingEntity.getStatisticsAt();
                // 해당 날짜의 통계 엔티티가 이미 있는지 확인
                StatisticsEntity statisticsEntity = statisticsEntityMap.get(statisticsAt);

                if (statisticsEntity == null) {
                    // 해당 날짜의 통계가 없으면 새로 생성
                    statisticsEntityMap.put(statisticsAt, StatisticsEntity.create(bookingEntity));
                } else {
                    // 이미 있으면 기존 통계에 값을 누적
                    statisticsEntity.add(bookingEntity);
                }
            }
            // Map에 모은 통계 엔티티를 리스트로 변환
            final List<StatisticsEntity> statisticsEntities = new ArrayList<>(statisticsEntityMap.values());
            // 통계 정보를 DB에 저장
            statisticsRepository.saveAll(statisticsEntities);
        };
    }

}