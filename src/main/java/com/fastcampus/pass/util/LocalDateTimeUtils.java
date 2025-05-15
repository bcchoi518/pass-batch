package com.fastcampus.pass.util;

import com.vladmihalcea.hibernate.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;

// 날짜와 시간 관련 유틸리티 메서드를 제공하는 클래스
public class LocalDateTimeUtils {
    // yyyy-MM-dd HH:mm 형식의 날짜/시간 포맷터 (예: 2024-06-01 12:30)
    public static final DateTimeFormatter YYYY_MM_DD_HH_MM = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    // yyyyMMdd 형식의 날짜 포맷터 (예: 20240601)
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyyMMdd");

    // LocalDateTime 객체를 "yyyy-MM-dd HH:mm" 문자열로 변환
    public static String format(final LocalDateTime localDateTime) {
        return localDateTime.format(YYYY_MM_DD_HH_MM);
    }

    // LocalDateTime 객체를 전달받은 포맷터(formatter)로 문자열 변환
    public static String format(final LocalDateTime localDateTime, DateTimeFormatter formatter) {
        return localDateTime.format(formatter);
    }

    // 문자열을 LocalDateTime 객체로 변환 (포맷: yyyy-MM-dd HH:mm)
    public static LocalDateTime parse(final String localDateTimeString) {
        // 입력 문자열이 비어있거나 null이면 null 반환
        if (StringUtils.isBlank(localDateTimeString)) {
            return null;
        }
        // 지정된 포맷(YYYY_MM_DD_HH_MM)으로 파싱
        return LocalDateTime.parse(localDateTimeString, YYYY_MM_DD_HH_MM);
    }

    // 해당 날짜가 1년 중 몇 번째 주(week of year)인지 반환
    public static int getWeekOfYear(final LocalDateTime localDateTime) {
        // Locale.KOREA 기준으로 주차 계산
        return localDateTime.get(WeekFields.of(Locale.KOREA).weekOfYear());
    }

}
