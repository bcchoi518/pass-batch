package com.fastcampus.pass.repository.pass;

import com.fastcampus.pass.repository.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity // JPA가 관리하는 엔티티임을 명시
@Table(name = "pass") // 매핑될 테이블명 지정
public class PassEntity extends BaseEntity {
    @Id // 기본키(primary key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임 (AUTO_INCREMENT)
    private Integer passSeq; // 이용권 고유 시퀀스
    private Integer packageSeq; // 패키지 고유 시퀀스(어떤 패키지에 속한 이용권인지)
    private String userId; // 이용자 ID

    @Enumerated(EnumType.STRING) // Enum을 문자열로 DB에 저장
    private PassStatus status; // 이용권 상태(READY, PROGRESSED, EXPIRED)
    private Integer remainingCount; // 남은 이용 횟수

    private LocalDateTime startedAt; // 이용권 사용 시작일시
    private LocalDateTime endedAt;   // 이용권 사용 종료일시
    private LocalDateTime expiredAt; // 이용권 만료일시

}
