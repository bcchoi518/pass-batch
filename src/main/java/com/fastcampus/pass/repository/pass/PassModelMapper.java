package com.fastcampus.pass.repository.pass;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

// MapStruct를 사용하여 객체 간 매핑을 자동화하는 Mapper 인터페이스
// @Mapper: MapStruct가 이 인터페이스의 구현체를 자동 생성하도록 지정
// unmappedTargetPolicy = ReportingPolicy.IGNORE: 매핑되지 않은 필드는 무시(에러 발생 X)
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PassModelMapper {
    // MapStruct가 생성한 구현체를 싱글턴으로 제공
    PassModelMapper INSTANCE = Mappers.getMapper(PassModelMapper.class);

    // @Mapping: 필드명이 다르거나, 커스텀 매핑이 필요할 때 사용
    // target = "status": PassEntity의 status 필드에 대해
    // qualifiedByName = "defaultStatus": 아래 defaultStatus 메서드를 사용해 매핑
    // target = "remainingCount": PassEntity의 remainingCount 필드에 대해
    // source = "bulkPassEntity.count": BulkPassEntity의 count 값을 매핑
    @Mapping(target = "status", qualifiedByName = "defaultStatus")
    @Mapping(target = "remainingCount", source = "bulkPassEntity.count")
    PassEntity toPassEntity(BulkPassEntity bulkPassEntity, String userId);

    // BulkPassStatus와 관계 없이 PassStatus값을 항상 READY로 설정하는 커스텀 매핑 메서드
    // @Named("defaultStatus"): 위 @Mapping에서 qualifiedByName으로 지정해 사용
    @Named("defaultStatus")
    default PassStatus status(BulkPassStatus status) {
        return PassStatus.READY;
    }

}
