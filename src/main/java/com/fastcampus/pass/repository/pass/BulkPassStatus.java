package com.fastcampus.pass.repository.pass;

// 대량 이용권의 상태를 나타내는 열거형(enum) 타입
public enum BulkPassStatus {
    READY,      // 대량 이용권이 준비된 상태(아직 처리 전)
    COMPLETED   // 대량 이용권이 처리 완료된 상태
}
