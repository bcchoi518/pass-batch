// 이 클래스는 ExpirePassesJobConfig의 만료 처리 배치 잡을 테스트하는 JUnit 테스트 클래스입니다.
package com.fastcampus.pass.job.pass;

import com.fastcampus.pass.config.TestBatchConfig;
import com.fastcampus.pass.repository.pass.PassEntity;
import com.fastcampus.pass.repository.pass.PassRepository;
import com.fastcampus.pass.repository.pass.PassStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j // 로그 출력을 위한 Lombok 어노테이션입니다.
@SpringBatchTest // Spring Batch 테스트를 위한 어노테이션입니다.
@SpringBootTest // Spring Boot 환경에서 테스트를 실행합니다.
@ActiveProfiles("test") // 테스트용 프로파일을 사용합니다.
@ContextConfiguration(classes = {ExpirePassesJobConfig.class, TestBatchConfig.class}) // 테스트에 필요한 설정 클래스를 지정합니다.
public class ExpirePassesJobConfigTest {
    @Autowired // JobLauncherTestUtils 빈을 주입받습니다.
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired // PassRepository 빈을 주입받습니다.
    private PassRepository passRepository;

    @Test // 만료 처리 Step의 동작을 검증하는 테스트입니다.
    public void test_expirePassesStep() throws Exception {
        // given: 만료 대상 PassEntity를 여러 개 추가합니다.
        addPassEntities(10);

        // when: 배치 잡을 실행합니다.
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();
        JobInstance jobInstance = jobExecution.getJobInstance();

        // then: 잡 실행 결과와 잡 이름이 기대와 일치하는지 확인합니다.
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());
        assertEquals("expirePassesJob", jobInstance.getJobName());

    }

    // 테스트용 PassEntity를 여러 개 추가하는 메서드입니다.
    private void addPassEntities(int size) {
        final LocalDateTime now = LocalDateTime.now();
        final Random random = new Random();

        List<PassEntity> passEntities = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            PassEntity passEntity = new PassEntity();
            passEntity.setPackageSeq(1);
            passEntity.setUserId("A" + 1000000 + i);
            passEntity.setStatus(PassStatus.PROGRESSED);
            passEntity.setRemainingCount(random.nextInt(11));
            passEntity.setStartedAt(now.minusDays(60));
            passEntity.setEndedAt(now.minusDays(1));
            passEntities.add(passEntity);

        }
        passRepository.saveAll(passEntities);

    }

}
