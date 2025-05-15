package com.fastcampus.pass.repository.packaze;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

// 패키지(PackageEntity) 엔티티에 대한 DB 접근을 담당하는 JPA Repository 인터페이스
public interface PackageRepository extends JpaRepository<PackageEntity, Integer> {
    // 생성일시(createdAt)가 특정 시점 이후인 패키지 목록을 페이징 처리하여 조회하는 쿼리 메서드
    // Pageable: 페이징 및 정렬 정보를 전달
    List<PackageEntity> findByCreatedAtAfter(LocalDateTime dateTime, Pageable pageable);

    // @Transactional: 트랜잭션 처리를 보장 (update, delete 등 데이터 변경 시 필요)
    // @Modifying: select가 아닌 update, delete 쿼리임을 명시
    // @Query: JPQL(객체지향 쿼리)로 직접 쿼리 작성
    // 패키지의 count(횟수)와 period(기간)를 packageSeq 기준으로 업데이트
    @Transactional
    @Modifying
    @Query(value = "UPDATE PackageEntity p " +
            "          SET p.count = :count," +
            "              p.period = :period" +
            "        WHERE p.packageSeq = :packageSeq")
    int updateCountAndPeriod(Integer packageSeq, Integer count, Integer period); // 업데이트된 행(row) 수 반환
}
