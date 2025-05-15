package com.fastcampus.pass.repository.notification;

import com.fastcampus.pass.repository.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Entity // JPA가 관리하는 엔티티임을 명시
@Table(name = "notification") // 매핑될 테이블명 지정
public class NotificationEntity extends BaseEntity {
    @Id // 기본키(primary key) 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기본 키 생성을 DB에 위임 (AUTO_INCREMENT)
    private Integer notificationSeq; // 알림 고유 시퀀스
    private String uuid;             // 알림 대상 사용자 UUID

    private NotificationEvent event; // 알림 이벤트 종류(예: 예약, 취소 등)
    private String text;             // 알림 내용(메시지)
    private boolean sent;            // 발송 여부(true: 발송됨, false: 미발송)
    private LocalDateTime sentAt;    // 알림 발송 일시

}
