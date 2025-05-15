package com.fastcampus.pass.repository;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;

// @MappedSuperclass: 이 클래스를 상속받는 엔티티 클래스에 공통 매핑 정보를 제공
@MappedSuperclass
// @EntityListeners: 엔티티의 생명주기 이벤트를 감지하여 처리(Auditing 기능 활성화)
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    // @CreatedDate: 엔티티가 처음 저장될 때 생성일시를 자동으로 저장
    // @Column(updatable = false, nullable = false): 생성일시는 한 번 저장되면 수정 불가, null 불가
    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;
    // @LastModifiedDate: 엔티티가 수정될 때마다 수정일시를 자동으로 갱신
    @LastModifiedDate
    private LocalDateTime modifiedAt;
}
