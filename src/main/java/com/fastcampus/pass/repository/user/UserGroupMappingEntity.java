package com.fastcampus.pass.repository.user;

import com.fastcampus.pass.repository.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Getter
@Setter
@ToString
@Entity // JPA가 관리하는 엔티티임을 명시
@Table(name = "user_group_mapping") // 매핑될 테이블명 지정
// @IdClass: 복합키(2개 이상의 필드로 이루어진 기본키) 사용 시 식별자 클래스를 지정
@IdClass(UserGroupMappingId.class)
public class UserGroupMappingEntity extends BaseEntity {
    @Id // 복합키의 일부(사용자 그룹 ID)
    private String userGroupId;
    @Id // 복합키의 일부(사용자 ID)
    private String userId;

    private String userGroupName; // 사용자 그룹 이름
    private String description;   // 사용자 그룹 설명

}
