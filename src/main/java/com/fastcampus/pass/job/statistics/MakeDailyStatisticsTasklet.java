package com.fastcampus.pass.job.statistics;

import com.fastcampus.pass.repository.statistics.AggregatedStatistics;
import com.fastcampus.pass.repository.statistics.StatisticsRepository;
import com.fastcampus.pass.util.CustomCSVWriter;
import com.fastcampus.pass.util.LocalDateTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// @Slf4j: 로그를 쉽게 남길 수 있도록 해주는 Lombok 어노테이션
@Slf4j
// @Component: Spring Bean으로 등록
@Component
// @StepScope: Step 실행 시점에 Bean이 생성됨(동적으로 파라미터 주입 가능)
@StepScope
public class MakeDailyStatisticsTasklet implements Tasklet {
    // 잡 파라미터에서 from, to 값을 주입받음
    @Value("#{jobParameters[from]}")
    private String fromString;
    @Value("#{jobParameters[to]}")
    private String toString;
    private final StatisticsRepository statisticsRepository; // 통계 데이터 접근용 Repository

    // 생성자 주입 방식으로 Repository를 주입받음
    public MakeDailyStatisticsTasklet(StatisticsRepository statisticsRepository) {
        this.statisticsRepository = statisticsRepository;
    }

    // Tasklet의 핵심 메서드: 한 번 실행되는 단일 작업 단위
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        // 파라미터 문자열을 LocalDateTime으로 변환
        final LocalDateTime from = LocalDateTimeUtils.parse(fromString);
        final LocalDateTime to = LocalDateTimeUtils.parse(toString);

        // 지정된 기간(from~to) 동안의 일별 통계 리스트 조회
        final List<AggregatedStatistics> statisticsList = statisticsRepository.findByStatisticsAtBetweenAndGroupBy(from, to);

        // CSV 파일로 저장할 데이터 준비
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"statisticsAt", "allCount", "attendedCount", "cancelledCount"}); // 헤더
        for (AggregatedStatistics statistics : statisticsList) {
            data.add(new String[]{
                    LocalDateTimeUtils.format(statistics.getStatisticsAt()), // 통계 일시
                    String.valueOf(statistics.getAllCount()),               // 전체 건수
                    String.valueOf(statistics.getAttendedCount()),          // 출석 건수
                    String.valueOf(statistics.getCancelledCount())          // 취소 건수
            });
        }
        // 파일명: daily_statistics_YYYYMMDD.csv
        CustomCSVWriter.write("daily_statistics_" + LocalDateTimeUtils.format(from, LocalDateTimeUtils.YYYY_MM_DD) + ".csv", data);
        return RepeatStatus.FINISHED; // 작업 종료
    }
}
