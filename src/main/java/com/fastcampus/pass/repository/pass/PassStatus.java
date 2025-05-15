package com.fastcampus.pass.repository.pass;

// 이용권의 상태를 나타내는 열거형(enum) 타입
public enum PassStatus {
    READY,      // 이용권이 준비된 상태(아직 사용 전)
    PROGRESSED, // 이용권이 사용 중인 상태
    EXPIRED     // 이용권이 만료된 상태
}
