package com.fastcampus.pass.repository.user;

// Lombok 어노테이션: getter, setter, toString 메서드를 자동 생성
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
// JPA에서 복합키(Composite Key)를 표현할 때 사용하는 식별자 클래스
public class UserGroupMappingId implements Serializable {
    // 사용자 그룹 ID (복합키의 일부)
    private String userGroupId;
    // 사용자 ID (복합키의 일부)
    private String userId;
}
