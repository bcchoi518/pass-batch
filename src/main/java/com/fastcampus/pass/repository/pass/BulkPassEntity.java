package com.fastcampus.pass.repository.pass;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity // JPA가 관리하는 엔티티임을 명시
@Table(name = "bulk_pass") // 매핑될 테이블명 지정
public class BulkPassEntity {
    @Id // 기본키(primary key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임 (AUTO_INCREMENT)
    private Integer bulkPassSeq; // 대량 이용권 고유 시퀀스
    private Integer packageSeq;  // 패키지 고유 시퀀스(어떤 패키지에 속한 대량 이용권인지)
    private String userGroupId;  // 사용자 그룹 ID(대상 그룹)

    @Enumerated(EnumType.STRING) // Enum을 문자열로 DB에 저장
    private BulkPassStatus status; // 대량 이용권 상태(READY, COMPLETED)
    private Integer count;         // 발급할 이용권 개수

    private LocalDateTime startedAt; // 이용권 사용 시작일시
    private LocalDateTime endedAt;   // 이용권 사용 종료일시

}
