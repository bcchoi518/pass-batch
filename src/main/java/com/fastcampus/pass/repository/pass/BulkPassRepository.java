package com.fastcampus.pass.repository.pass;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

// 대량 이용권(BulkPassEntity) 엔티티에 대한 DB 접근을 담당하는 JPA Repository 인터페이스
public interface BulkPassRepository extends JpaRepository<BulkPassEntity, Integer> {
    // 상태(status)와 시작일시(startedAt)가 특정 조건을 만족하는 대량 이용권 목록을 조회하는 쿼리 메서드
    // findByStatusAndStartedAtGreaterThan: Spring Data JPA가 메서드 이름을 분석해 자동으로 쿼리를 생성
    // 파라미터: status(이용권 상태), startedAt(시작일시)
    // 반환 타입: BulkPassEntity 리스트
    List<BulkPassEntity> findByStatusAndStartedAtGreaterThan(BulkPassStatus status, LocalDateTime startedAt);
}
