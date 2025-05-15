package com.fastcampus.pass.job.pass;

import com.fastcampus.pass.repository.pass.*;
import com.fastcampus.pass.repository.user.UserGroupMappingEntity;
import com.fastcampus.pass.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

// 이 클래스는 AddPassesTasklet의 동작을 검증하는 JUnit 테스트 클래스입니다.
@Slf4j // 로그 출력을 위한 Lombok 어노테이션입니다.
@ExtendWith(MockitoExtension.class) // Mockito를 사용한 단위 테스트를 위해 JUnit5 확장 기능을 적용합니다.
public class AddPassesTaskletTest {
    @Mock // StepContribution 객체를 Mock으로 생성합니다.
    private StepContribution stepContribution;

    @Mock // ChunkContext 객체를 Mock으로 생성합니다.
    private ChunkContext chunkContext;

    @Mock // PassRepository를 Mock으로 생성합니다.
    private PassRepository passRepository;

    @Mock // BulkPassRepository를 Mock으로 생성합니다.
    private BulkPassRepository bulkPassRepository;

    @Mock // UserGroupMappingRepository를 Mock으로 생성합니다.
    private UserGroupMappingRepository userGroupMappingRepository;

    // @InjectMocks는 AddPassesTasklet 인스턴스를 생성하고 위의 Mock 객체들을 주입합니다.
    @InjectMocks
    private AddPassesTasklet addPassesTasklet;

    @Test // AddPassesTasklet의 execute 메서드 테스트
    public void test_execute() {
        // given: 테스트에 사용할 BulkPassEntity, UserGroupMappingEntity를 준비합니다.
        final String userGroupId = "GROUP";
        final String userId = "A1000000";
        final Integer packageSeq = 1;
        final Integer count = 10;

        final LocalDateTime now = LocalDateTime.now();

        final BulkPassEntity bulkPassEntity = new BulkPassEntity();
        bulkPassEntity.setPackageSeq(packageSeq);
        bulkPassEntity.setUserGroupId(userGroupId);
        bulkPassEntity.setStatus(BulkPassStatus.READY);
        bulkPassEntity.setCount(count);
        bulkPassEntity.setStartedAt(now);
        bulkPassEntity.setEndedAt(now.plusDays(60));

        final UserGroupMappingEntity userGroupMappingEntity = new UserGroupMappingEntity();
        userGroupMappingEntity.setUserGroupId(userGroupId);
        userGroupMappingEntity.setUserId(userId);

        // when: Mock 객체의 동작을 지정합니다.
        when(bulkPassRepository.findByStatusAndStartedAtGreaterThan(eq(BulkPassStatus.READY), any())).thenReturn(List.of(bulkPassEntity));
        when(userGroupMappingRepository.findByUserGroupId(eq("GROUP"))).thenReturn(List.of(userGroupMappingEntity));

        // 실제로 Tasklet의 execute를 호출합니다.
        RepeatStatus repeatStatus = addPassesTasklet.execute(stepContribution, chunkContext);

        // then: execute의 return 값과 저장된 PassEntity의 값을 검증합니다.
        assertEquals(RepeatStatus.FINISHED, repeatStatus); // execute의 return 값이 FINISHED인지 확인

        // 추가된 PassEntity 값을 확인합니다.
        ArgumentCaptor<List> passEntitiesCaptor = ArgumentCaptor.forClass(List.class);
        verify(passRepository, times(1)).saveAll(passEntitiesCaptor.capture());
        final List<PassEntity> passEntities = passEntitiesCaptor.getValue();

        assertEquals(1, passEntities.size()); // PassEntity가 1개 추가되었는지 확인

        final PassEntity passEntity = passEntities.get(0);
        assertEquals(packageSeq, passEntity.getPackageSeq());
        assertEquals(userId, passEntity.getUserId());
        assertEquals(PassStatus.READY, passEntity.getStatus());
        assertEquals(count, passEntity.getRemainingCount());

    }

}
