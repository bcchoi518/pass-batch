package com.fastcampus.pass.repository.packaze;

import com.fastcampus.pass.repository.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;

@Getter
@Setter
@ToString
@Entity // JPA가 관리하는 엔티티임을 명시
@Table(name = "package") // 매핑될 테이블명 지정
public class PackageEntity extends BaseEntity {
    @Id // 기본키(primary key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임 (AUTO_INCREMENT)
    private Integer packageSeq; // 패키지 고유 시퀀스

    private String packageName; // 패키지 이름
    private Integer count;      // 패키지에 포함된 이용권 횟수
    private Integer period;     // 패키지 유효 기간(일 단위 등)

}
