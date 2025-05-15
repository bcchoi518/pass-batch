package com.fastcampus.pass.repository.booking;

// 예약의 상태를 나타내는 열거형(enum) 타입
public enum BookingStatus {
    READY,      // 예약이 준비된 상태(아직 시작 전)
    PROGRESSED, // 예약이 진행 중인 상태
    COMPLETED,  // 예약이 완료된 상태
    CANCELLED   // 예약이 취소된 상태
}
