package com.fastcampus.pass.adapter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class KakaoTalkMessageResponse {
    // 카카오톡 메시지 API의 successful_receiver_uuids 필드와 매핑
    // 메시지 전송에 성공한 수신자 UUID 리스트
    @JsonProperty("successful_receiver_uuids")
    private List<String> successfulReceiverUuids;

}
