package com.fastcampus.pass.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// 사용자 그룹과 사용자 매핑 정보를 DB에서 조회/저장/수정/삭제할 수 있게 해주는 JPA Repository 인터페이스
public interface UserGroupMappingRepository extends JpaRepository<UserGroupMappingEntity, Integer> {
    // 사용자 그룹 ID로 매핑된 모든 엔티티를 조회하는 쿼리 메서드
    // findByUserGroupId: Spring Data JPA가 메서드 이름을 분석해 자동으로 쿼리를 생성
    // 반환 타입: UserGroupMappingEntity 리스트
    List<UserGroupMappingEntity> findByUserGroupId(String userGroupId);
}
