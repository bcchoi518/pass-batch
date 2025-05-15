package com.fastcampus.pass.repository.statistics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 자동 생성
// 통계 데이터를 일 단위로 집계하여 보관하는 클래스
public class AggregatedStatistics {
    private LocalDateTime statisticsAt; // 통계 기준 일시(일 단위)
    private long allCount;              // 전체 건수
    private long attendedCount;         // 출석(참여) 건수
    private long cancelledCount;        // 취소 건수

    // 다른 AggregatedStatistics 객체의 값을 현재 객체에 누적(합산)하는 메서드
    public void merge(final AggregatedStatistics statistics) {
        this.allCount += statistics.getAllCount();           // 전체 건수 누적
        this.attendedCount += statistics.getAttendedCount(); // 출석 건수 누적
        this.cancelledCount += statistics.getCancelledCount(); // 취소 건수 누적
    }
}
