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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// @Slf4j: 로그를 쉽게 남길 수 있도록 해주는 Lombok 어노테이션
@Slf4j
// @Component: Spring Bean으로 등록
@Component
// @StepScope: Step 실행 시점에 Bean이 생성됨(동적으로 파라미터 주입 가능)
@StepScope
public class MakeWeeklyStatisticsTasklet implements Tasklet {
    // 잡 파라미터에서 from, to 값을 주입받음
    @Value("#{jobParameters[from]}")
    private String fromString;
    @Value("#{jobParameters[to]}")
    private String toString;

    // 통계 데이터 접근용 Repository (생성자 주입)
    private final StatisticsRepository statisticsRepository;

    // 생성자 주입 방식으로 Repository를 주입받음
    public MakeWeeklyStatisticsTasklet(StatisticsRepository statisticsRepository) {
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
        // 주차별 집계 결과를 저장할 Map (week -> AggregatedStatistics)
        Map<Integer, AggregatedStatistics> weeklyStatisticsEntityMap = new LinkedHashMap<>();

        // 각 일별 통계를 주차별로 합산
        for (AggregatedStatistics statistics : statisticsList) {
            int week = LocalDateTimeUtils.getWeekOfYear(statistics.getStatisticsAt()); // 몇 번째 주인지 계산
            AggregatedStatistics savedStatisticsEntity = weeklyStatisticsEntityMap.get(week);

            if (savedStatisticsEntity == null) {
                // 해당 주에 첫 데이터면 바로 저장
                weeklyStatisticsEntityMap.put(week, statistics);
            } else {
                // 이미 있으면 누적 합산
                savedStatisticsEntity.merge(statistics);
            }
        }

        // CSV 파일로 저장할 데이터 준비
        List<String[]> data = new ArrayList<>();
        data.add(new String[]{"week", "allCount", "attendedCount", "cancelledCount"}); // 헤더
        weeklyStatisticsEntityMap.forEach((week, statistics) -> {
            data.add(new String[]{
                    "Week " + week,
                    String.valueOf(statistics.getAllCount()),
                    String.valueOf(statistics.getAttendedCount()),
                    String.valueOf(statistics.getCancelledCount())
            });
        });
        // 파일명: weekly_statistics_YYYYMMDD.csv
        CustomCSVWriter.write("weekly_statistics_" + LocalDateTimeUtils.format(from, LocalDateTimeUtils.YYYY_MM_DD) + ".csv", data);
        return RepeatStatus.FINISHED; // 작업 종료
    }
}
