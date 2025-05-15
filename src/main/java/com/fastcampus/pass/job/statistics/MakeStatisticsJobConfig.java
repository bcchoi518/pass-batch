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

    // JPA의 EntityManagerFactory로, DB 접근에 사용됩니다.
    private final EntityManagerFactory entityManagerFactory;
    // 통계 정보를 저장하는 JPA Repository입니다.
    private final StatisticsRepository statisticsRepository;
    // 일간 통계 처리를 위한 Tasklet입니다.
    private final MakeDailyStatisticsTasklet makeDailyStatisticsTasklet;
    // 주간 통계 처리를 위한 Tasklet입니다.
    private final MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet;

    // 생성자에서 EntityManagerFactory, StatisticsRepository, Tasklet만 주입받음
    public MakeStatisticsJobConfig(EntityManagerFactory entityManagerFactory, StatisticsRepository statisticsRepository, MakeDailyStatisticsTasklet makeDailyStatisticsTasklet, MakeWeeklyStatisticsTasklet makeWeeklyStatisticsTasklet) {
        this.entityManagerFactory = entityManagerFactory;
        this.statisticsRepository = statisticsRepository;
        this.makeDailyStatisticsTasklet = makeDailyStatisticsTasklet;
        this.makeWeeklyStatisticsTasklet = makeWeeklyStatisticsTasklet;
    }

    @Bean
    public Step addStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("addStatisticsStep", jobRepository)
                .<BookingEntity, BookingEntity>chunk(CHUNK_SIZE, transactionManager)
                .reader(addStatisticsItemReader(null, null))
                .writer(addStatisticsItemWriter())
                .build();
    }

    @Bean
    public Step makeDailyStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("makeDailyStatisticsStep", jobRepository)
                .tasklet(makeDailyStatisticsTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step makeWeeklyStatisticsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("makeWeeklyStatisticsStep", jobRepository)
                .tasklet(makeWeeklyStatisticsTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job makeStatisticsJob(JobRepository jobRepository, Step addStatisticsStep, Step makeDailyStatisticsStep, Step makeWeeklyStatisticsStep) {
        Flow addStatisticsFlow = new FlowBuilder<Flow>("addStatisticsFlow")
                .start(addStatisticsStep)
                .build();
        Flow makeDailyStatisticsFlow = new FlowBuilder<Flow>("makeDailyStatisticsFlow")
                .start(makeDailyStatisticsStep)
                .build();
        Flow makeWeeklyStatisticsFlow = new FlowBuilder<Flow>("makeWeeklyStatisticsFlow")
                .start(makeWeeklyStatisticsStep)
                .build();
        Flow parallelMakeStatisticsFlow = new FlowBuilder<Flow>("parallelMakeStatisticsFlow")
                .split(new SimpleAsyncTaskExecutor())
                .add(makeDailyStatisticsFlow, makeWeeklyStatisticsFlow)
                .build();
        return new JobBuilder("makeStatisticsJob", jobRepository)
                .start(addStatisticsFlow)
                .next(parallelMakeStatisticsFlow)
                .build()
                .build();
    }

    // Step 실행 시점에 파라미터를 주입받기 위해 @StepScope를 사용합니다.
    @Bean
    @StepScope
    public JpaCursorItemReader<BookingEntity> addStatisticsItemReader(
            @Value("#{jobParameters[from]}") String fromString,
            @Value("#{jobParameters[to]}") String toString
    ) {
        // 문자열로 받은 날짜 파라미터를 LocalDateTime으로 변환합니다.
        final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
        final LocalDateTime to = LocalDateTimeUtils.parse(toString);

        // JPA를 이용해 BookingEntity를 읽어오는 ItemReader를 생성합니다.
        return new JpaCursorItemReaderBuilder<BookingEntity>()
                .name("addStatisticsItemReader") // 리더 이름 지정
                .entityManagerFactory(entityManagerFactory) // DB 연결 정보
                .queryString("select b from BookingEntity b where b.endedAt between :from and :to") // 기간 내 예약 정보만 조회
                .parameterValues(Map.of("from", from, "to", to)) // 쿼리 파라미터 바인딩
                .build();

    }

    // 예약 정보를 통계로 변환하여 저장하는 Writer를 등록합니다.
    @Bean
    public ItemWriter<BookingEntity> addStatisticsItemWriter() {
        return bookingEntities -> {
            // 날짜별로 통계 정보를 모으기 위한 Map입니다.
            Map<LocalDateTime, StatisticsEntity> statisticsEntityMap = new LinkedHashMap<>();

            // 예약 엔티티 리스트를 순회합니다.
            for (BookingEntity bookingEntity: bookingEntities) {
                // 예약의 통계 기준 날짜를 가져옵니다.
                final LocalDateTime statisticsAt = bookingEntity.getStatisticsAt();
                // 해당 날짜의 통계 엔티티가 이미 있는지 확인합니다.
                StatisticsEntity statisticsEntity = statisticsEntityMap.get(statisticsAt);

                if (statisticsEntity == null) {
                    // 해당 날짜의 통계가 없으면 새로 생성합니다.
                    statisticsEntityMap.put(statisticsAt, StatisticsEntity.create(bookingEntity));
                } else {
                    // 이미 있으면 기존 통계에 값을 누적합니다.
                    statisticsEntity.add(bookingEntity);
                }
            }
            // Map에 모은 통계 엔티티를 리스트로 변환합니다.
            final List<StatisticsEntity> statisticsEntities = new ArrayList<>(statisticsEntityMap.values());
            // 통계 정보를 DB에 저장합니다.
            statisticsRepository.saveAll(statisticsEntities);
        };

    }

}