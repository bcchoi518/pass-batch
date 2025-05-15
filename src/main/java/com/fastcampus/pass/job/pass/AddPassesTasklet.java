package com.fastcampus.pass.job.pass;

import com.fastcampus.pass.repository.pass.*;
import com.fastcampus.pass.repository.user.UserGroupMappingEntity;
import com.fastcampus.pass.repository.user.UserGroupMappingRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AddPassesTasklet implements Tasklet {
    private final PassRepository passRepository;
    private final BulkPassRepository bulkPassRepository;
    private final UserGroupMappingRepository userGroupMappingRepository;

    public AddPassesTasklet(PassRepository passRepository, BulkPassRepository bulkPassRepository, UserGroupMappingRepository userGroupMappingRepository) {
        this.passRepository = passRepository;
        this.bulkPassRepository = bulkPassRepository;
        this.userGroupMappingRepository = userGroupMappingRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // 1일 전을 기준으로 대량 이용권을 조회하기 위한 날짜 생성
        final LocalDateTime startedAt = LocalDateTime.now().minusDays(1); // 기준일(1일 전)
        // READY 상태이면서 시작일이 기준일보다 큰 대량 이용권 목록 조회
        final List<BulkPassEntity> bulkPassEntities = bulkPassRepository.findByStatusAndStartedAtGreaterThan(BulkPassStatus.READY, startedAt);

        int count = 0; // 추가된 이용권 개수 카운트
        // 대량 이용권 정보를 하나씩 순회
        for(BulkPassEntity bulkPassEntity: bulkPassEntities) {
            // 해당 user group에 속한 userId 목록 조회
            final List<String> userIds = userGroupMappingRepository.findByUserGroupId(bulkPassEntity.getUserGroupId())
                    .stream().map(UserGroupMappingEntity::getUserId).toList();

            // 각 userId에 대해 이용권 추가 및 카운트 누적
            count += addPasses(bulkPassEntity, userIds);

            // 대량 이용권 상태를 COMPLETED로 변경
            bulkPassEntity.setStatus(BulkPassStatus.COMPLETED);
        }

        // 로그로 결과 출력
        log.info("AddPassesTasklet - execute: 이용권 {}건 추가 완료, startedAt={}", count, startedAt);
        // 배치가 정상적으로 끝났음을 알림
        return RepeatStatus.FINISHED;
    }

    // bulkPass의 정보와 userId 목록을 받아 각 userId에 대한 pass 데이터를 생성하고 저장
    private int addPasses(BulkPassEntity bulkPassEntity, List<String> userIds) {
        List<PassEntity> passEntities = new ArrayList<>(); // 생성할 이용권 리스트
        // 각 userId에 대해 반복
        for(String userId: userIds) {
            // 대량 이용권 정보와 userId로 PassEntity 생성
            PassEntity passEntity = PassModelMapper.INSTANCE.toPassEntity(bulkPassEntity, userId);
            passEntities.add(passEntity); // 리스트에 추가
        }
        // 생성된 이용권을 DB에 저장하고 저장된 개수 반환
        return passRepository.saveAll(passEntities).size();
    }
}
