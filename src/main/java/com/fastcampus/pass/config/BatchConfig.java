package com.fastcampus.pass.config;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

// Spring Batch 기능을 활성화하는 설정 클래스
@EnableBatchProcessing // 배치 작업에 필요한 주요 Bean(JobBuilderFactory, StepBuilderFactory 등)을 자동으로 등록
@Configuration // Spring 설정 클래스임을 명시
public class BatchConfig {
    // 별도의 설정이 없더라도, 이 클래스가 존재하면 Spring Batch가 활성화됨
}
