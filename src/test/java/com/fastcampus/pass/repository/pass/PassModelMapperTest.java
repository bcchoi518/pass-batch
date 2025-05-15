package com.fastcampus.pass.repository.pass;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

// 이 클래스는 PassModelMapper의 동작을 검증하는 JUnit 테스트 클래스입니다.
public class PassModelMapperTest {

    @Test // PassModelMapper의 toPassEntity 메서드 테스트
    public void test_toPassEntity() {
        // given: 테스트에 사용할 BulkPassEntity와 userId를 준비합니다.
        final LocalDateTime now = LocalDateTime.now();
        final String userId = "A1000000";

        BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setPackageSeq(1);
        bulkPassEntity.setUserGroupId("GROUP");
        bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
        bulkPassEntity.setCount(10);
        bulkPassEntity.setStartedAt(now.minusDays(60));
        bulkPassEntity.setEndedAt(now);

        // when: BulkPassEntity와 userId로 PassEntity를 생성합니다.
        final PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);

        // then: 생성된 PassEntity의 값이 기대와 일치하는지 검증합니다.
        assertEquals(1, passEntity.getPackageSeq());
        assertEquals(PassStatus.READY, passEntity.getStatus());
        assertEquals(10, passEntity.getRemainingCount());
        assertEquals(now.minusDays(60), passEntity.getStartedAt());
        assertEquals(now, passEntity.getEndedAt());
        assertEquals(userId, passEntity.getUserId());

    }
}
