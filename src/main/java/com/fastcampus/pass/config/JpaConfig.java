package com.fastcampus.pass.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// JPA Auditing(자동 생성/수정일 관리) 기능을 활성화하는 설정 클래스
@EnableJpaAuditing // @CreatedDate, @LastModifiedDate 등 자동 관리 기능 활성화
@Configuration // Spring 설정 클래스임을 명시
public class JpaConfig {
    // 별도의 설정이 없더라도, 이 클래스가 존재하면 JPA Auditing이 활성화됨
}
