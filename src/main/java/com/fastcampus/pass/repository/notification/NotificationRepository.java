package com.fastcampus.pass.repository.notification;

import org.springframework.data.jpa.repository.JpaRepository;

// 알림(NotificationEntity) 엔티티에 대한 기본적인 CRUD(생성, 조회, 수정, 삭제) 기능을 제공하는 JPA Repository 인터페이스
// JpaRepository<NotificationEntity, Integer>: NotificationEntity를 관리하며, 기본키 타입은 Integer임을 명시
public interface NotificationRepository extends JpaRepository<NotificationEntity, Integer> {
    // 별도의 메서드 선언 없이도 JpaRepository가 기본적인 DB 연산을 모두 제공
}
