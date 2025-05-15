package com.fastcampus.pass.repository.booking;

import com.fastcampus.pass.repository.BaseEntity;
import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.user.UserEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity // JPA가 관리하는 엔티티임을 명시
@Table(name = "booking") // 매핑될 테이블명 지정
public class BookingEntity extends BaseEntity {
    @Id // 기본키(primary key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임 (AUTO_INCREMENT)
    private Integer bookingSeq; // 예약 고유 시퀀스
    private Integer passSeq;    // 이용권 고유 시퀀스(예약에 연결된 이용권)
    private String userId;      // 사용자 ID

    @Enumerated(EnumType.STRING) // Enum을 문자열로 DB에 저장
    private BookingStatus status; // 예약 상태(예: 예약됨, 취소 등)
    private boolean usedPass;     // 이용권 사용 여부
    private boolean attended;     // 출석 여부

    private LocalDateTime startedAt;   // 예약 시작일시
    private LocalDateTime endedAt;     // 예약 종료일시
    private LocalDateTime cancelledAt; // 예약 취소일시

    // 예약과 사용자(UserEntity) 간 다대일(N:1) 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private UserEntity userEntity;

    // 예약과 이용권(PassEntity) 간 다대일(N:1) 연관관계 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "passSeq", insertable = false, updatable = false)
    private PassEntity passEntity;

    // 통계 집계 시 사용할 날짜(endedAt의 날짜만, 시간은 00:00:00으로 맞춤)
    // 예: 2024-06-01 00:00:00
    public LocalDateTime getStatisticsAt() {
        return this.endedAt.withHour(0).withMinute(0).withSecond(0).withNano(0);
    }

}
