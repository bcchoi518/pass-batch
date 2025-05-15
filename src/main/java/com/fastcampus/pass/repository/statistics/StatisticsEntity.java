package com.fastcampus.pass.repository.statistics;

import com.fastcampus.pass.repository.booking.BookingEntity;
import com.fastcampus.pass.repository.booking.BookingStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity // JPA가 관리하는 엔티티임을 명시
@Table(name = "statistics") // 매핑될 테이블명 지정
public class StatisticsEntity {
    @Id // 기본키(primary key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임 (AUTO_INCREMENT)
    private Integer statisticsSeq; // 통계 시퀀스(고유값)
    private LocalDateTime statisticsAt; // 통계 기준 일시(일 단위)

    private int allCount;        // 전체 예약 건수
    private int attendedCount;   // 출석(참여) 건수
    private int cancelledCount;  // 취소 건수

    // BookingEntity로부터 StatisticsEntity를 생성하는 정적 팩토리 메서드
    public static StatisticsEntity create(final BookingEntity bookingEntity) {
        StatisticsEntity statisticsEntity = new StatisticsEntity();
        statisticsEntity.setStatisticsAt(bookingEntity.getStatisticsAt()); // 통계 기준 일시 설정
        statisticsEntity.setAllCount(1); // 예약 1건 생성
        if (bookingEntity.isAttended()) {
            statisticsEntity.setAttendedCount(1); // 출석 시 출석 건수 1로 설정
        }
        if (BookingStatus.CANCELLED.equals(bookingEntity.getStatus())) {
            statisticsEntity.setCancelledCount(1); // 취소 시 취소 건수 1로 설정
        }
        return statisticsEntity;
    }

    // BookingEntity 정보를 누적하여 통계값을 증가시키는 메서드
    public void add(final BookingEntity bookingEntity) {
        this.allCount++; // 전체 예약 건수 증가
        if (bookingEntity.isAttended()) {
            this.attendedCount++; // 출석 시 출석 건수 증가
        }
        if (BookingStatus.CANCELLED.equals(bookingEntity.getStatus())) {
            this.cancelledCount++; // 취소 시 취소 건수 증가
        }
    }

}
