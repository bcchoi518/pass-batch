// 이 클래스는 PackageRepository의 주요 기능을 테스트하는 JUnit 테스트 클래스입니다.
package com.fastcampus.pass.repository.packaze;

import com.fastcampus.pass.repository.packaze.PackageEntity;
import com.fastcampus.pass.repository.packaze.PackageRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

//@DataJpaTest

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
public class PackageRepositoryTest {
    @Autowired
    private PackageRepository packageRepository;

    @Test
    public void test_save() {
        // given: 테스트에 사용할 패키지 엔티티를 생성합니다.
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디 챌린지 PT 12주");
        packageEntity.setPeriod(84);

        // when: 엔티티를 저장합니다.
        packageRepository.save(packageEntity);

        // then: 저장된 엔티티의 PK가 null이 아닌지 확인합니다.
        assertNotNull(packageEntity.getPackageSeq());

    }

    @Test
    public void test_findByCreatedAtAfter() {
        // given: 기준 시간 이후에 생성된 패키지 엔티티 2개를 저장합니다.
        LocalDateTime dateTime = LocalDateTime.now().minusMinutes(1);

        PackageEntity packageEntity0 = new PackageEntity();
        packageEntity0.setPackageName("학생 전용 3개월");
        packageEntity0.setPeriod(90);
        packageRepository.save(packageEntity0);

        PackageEntity packageEntity1 = new PackageEntity();
        packageEntity1.setPackageName("학생 전용 6개월");
        packageEntity1.setPeriod(180);
        packageRepository.save(packageEntity1);

        // when: 기준 시간 이후의 패키지 중 가장 최근 것 1개만 조회합니다.
        final List<PackageEntity> packageEntities = packageRepository.findByCreatedAtAfter(dateTime, PageRequest.of(0, 1, Sort.by("packageSeq").descending()));

        // then: 조회 결과가 1개이고, 가장 최근에 저장한 엔티티와 일치하는지 확인합니다.
        assertEquals(1, packageEntities.size());
        assertEquals(packageEntity1.getPackageSeq(), packageEntities.get(0).getPackageSeq());

    }

    @Test
    public void test_updateCountAndPeriod() {
        // given: 테스트에 사용할 패키지 엔티티를 저장합니다.
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("바디프로필 이벤트 4개월");
        packageEntity.setPeriod(90);
        packageRepository.save(packageEntity);

        // when: count와 period를 수정합니다.
        int updatedCount = packageRepository.updateCountAndPeriod(packageEntity.getPackageSeq(), 30, 120);
        final PackageEntity updatedPackageEntity = packageRepository.findById(packageEntity.getPackageSeq()).get();

        // then: 수정된 값이 정상적으로 반영되었는지 확인합니다.
        assertEquals(1, updatedCount);
        assertEquals(30, updatedPackageEntity.getCount());
        assertEquals(120, updatedPackageEntity.getPeriod());

    }

    @Test
    public void test_delete() {
        // given: 테스트에 사용할 패키지 엔티티를 저장합니다.
        PackageEntity packageEntity = new PackageEntity();
        packageEntity.setPackageName("제거할 이용권");
        packageEntity.setCount(1);
        PackageEntity newPackageEntity = packageRepository.save(packageEntity);

        // when: 엔티티를 삭제합니다.
        packageRepository.deleteById(newPackageEntity.getPackageSeq());

        // then: 삭제 후 해당 엔티티가 존재하지 않는지 확인합니다.
        assertTrue(packageRepository.findById(newPackageEntity.getPackageSeq()).isEmpty());

    }
}
