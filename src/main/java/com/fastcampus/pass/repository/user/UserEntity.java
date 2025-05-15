package com.fastcampus.pass.repository.user;

import com.fastcampus.pass.repository.BaseEntity;
import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import jakarta.persistence.*;
import java.util.Map;

@Getter
@Setter
@ToString
@Entity // JPA가 관리하는 엔티티임을 명시
@Table(name = "user") // 매핑될 테이블명 지정
// json의 타입을 정의합니다. (Hibernate에서 JSON 타입을 지원하도록 설정)
@TypeDef(name = "json", typeClass = JsonType.class)
public class UserEntity extends BaseEntity {
    @Id // 기본키(primary key) 지정
    private String userId; // 사용자 고유 ID

    private String userName; // 사용자 이름
    @Enumerated(EnumType.STRING) // Enum을 문자열로 DB에 저장
    private UserStatus status; // 사용자 상태 (ACTIVE, INACTIVE)
    private String phone; // 사용자 전화번호

    // json 형태로 저장되어 있는 문자열 데이터를 Map으로 매핑합니다.
    // @Type(type = "json"): 해당 필드를 JSON 타입으로 매핑
    @Type(type = "json")
    private Map<String, Object> meta; // 추가 정보(키-값 쌍)

    // meta 필드에서 "uuid" 값을 추출하는 메서드
    public String getUuid() {
        String uuid = null;
        // meta에 "uuid" 키가 있으면 값을 문자열로 변환하여 반환
        if (meta.containsKey("uuid")) {
            uuid = String.valueOf(meta.get("uuid"));
        }
        return uuid;
    }

}
