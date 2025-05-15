package com.fastcampus.pass.repository.notification;

import com.fastcampus.pass.repository.booking.BookingEntity;
import com.fastcampus.pass.util.LocalDateTimeUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

// MapStruct를 사용하여 객체 간 매핑을 자동화하는 Mapper 인터페이스
// @Mapper: MapStruct가 이 인터페이스의 구현체를 자동 생성하도록 지정
// unmappedTargetPolicy = ReportingPolicy.IGNORE: 매핑되지 않은 필드는 무시(에러 발생 X)
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationModelMapper {
    // MapStruct가 생성한 구현체를 싱글턴으로 제공
    NotificationModelMapper INSTANCE = Mappers.getMapper(NotificationModelMapper.class);

    // @Mapping: 필드명이 다르거나, 커스텀 매핑이 필요할 때 사용
    // target = "uuid": NotificationEntity의 uuid 필드에 bookingEntity.userEntity.uuid 값을 매핑
    // target = "text": NotificationEntity의 text 필드에 bookingEntity.startedAt 값을 아래 text 메서드로 변환하여 매핑
    @Mapping(target = "uuid", source = "bookingEntity.userEntity.uuid")
    @Mapping(target = "text", source = "bookingEntity.startedAt", qualifiedByName = "text")
    NotificationEntity toNotificationEntity(BookingEntity bookingEntity, NotificationEvent event);

    // 알람 보낼 메시지 생성 (수업 시작 시간 포함)
    // @Named("text"): 위 @Mapping에서 qualifiedByName으로 지정해 사용
    @Named("text")
    default String text(LocalDateTime startedAt) {
        // LocalDateTimeUtils.format을 사용해 날짜/시간을 문자열로 변환하여 메시지에 포함
        return String.format("안녕하세요. %s 수업 시작합니다. 수업 전 출석 체크 부탁드립니다. \uD83D\uDE0A", LocalDateTimeUtils.format(startedAt));
    }

}
