package com.fastcampus.pass.repository.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;

// 예약(BookingEntity) 엔티티에 대한 DB 접근을 담당하는 JPA Repository 인터페이스
public interface BookingRepository extends JpaRepository<BookingEntity, Integer> {
    // @Transactional: 트랜잭션 처리를 보장 (update, delete 등 데이터 변경 시 필요)
    // @Modifying: select가 아닌 update, delete 쿼리임을 명시
    // @Query: JPQL(객체지향 쿼리)로 직접 쿼리 작성
    // 예약의 usedPass(이용권 사용 여부)와 수정일시(modifiedAt)를 passSeq 기준으로 업데이트
    @Transactional
    @Modifying
    @Query(value = "UPDATE BookingEntity b" +
            "          SET b.usedPass = :usedPass," +
            "              b.modifiedAt = CURRENT_TIMESTAMP" +
            "        WHERE b.passSeq = :passSeq")
    int updateUsedPass(Integer passSeq, boolean usedPass); // 업데이트된 행(row) 수 반환
}
