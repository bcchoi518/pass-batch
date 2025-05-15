package com.fastcampus.pass.repository.statistics;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// 통계(StatisticsEntity) 엔티티에 대한 DB 접근을 담당하는 JPA Repository 인터페이스
public interface StatisticsRepository extends JpaRepository<StatisticsEntity, Integer> {

    // @Query 어노테이션을 사용하여 JPQL(객체지향 쿼리)로 직접 쿼리를 작성
    // 통계 일시(statisticsAt)가 특정 기간(from~to) 사이에 있는 데이터들을 일자별로 집계하여 반환
    // AggregatedStatistics: 집계 결과를 담는 DTO
    // :from, :to는 메서드 파라미터와 바인딩됨
    @Query(value = "SELECT new com.fastcampus.pass.repository.statistics.AggregatedStatistics(s.statisticsAt, SUM(s.allCount), SUM(s.attendedCount), SUM(s.cancelledCount)) " +
            "         FROM StatisticsEntity s " +
            "        WHERE s.statisticsAt BETWEEN :from AND :to " +
            "     GROUP BY s.statisticsAt")
    List<AggregatedStatistics> findByStatisticsAtBetweenAndGroupBy(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
    // 반환 타입: 집계된 통계 리스트
}
