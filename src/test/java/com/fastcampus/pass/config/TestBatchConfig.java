// 이 클래스는 테스트 환경에서 사용할 Batch, JPA, 트랜잭션 관련 설정을 담당합니다.
package com.fastcampus.pass.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration // 이 클래스가 스프링 설정 클래스임을 나타냅니다.
@EnableJpaAuditing // JPA Auditing 기능을 활성화합니다.
@EnableAutoConfiguration // Spring Boot의 자동 설정을 활성화합니다.
@EnableBatchProcessing // Spring Batch 기능을 활성화합니다.
@EntityScan("com.fastcampus.pass.repository") // JPA 엔티티 스캔 범위를 지정합니다.
@EnableJpaRepositories("com.fastcampus.pass.repository") // JPA 레포지토리 스캔 범위를 지정합니다.
@EnableTransactionManagement // 트랜잭션 관리를 활성화합니다.
public class TestBatchConfig {
    // 별도의 설정이나 빈 등록 없이 어노테이션 기반 설정만 사용합니다.
}
