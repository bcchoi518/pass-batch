package com.fastcampus.pass.adapter.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collections;
import java.util.List;

@Getter
@Setter
@ToString
public class KakaoTalkMessageRequest {
    // 카카오톡 메시지 API의 template_object 필드와 매핑
    @JsonProperty("template_object")
    private TemplateObject templateObject;

    // 카카오톡 메시지 API의 receiver_uuids 필드와 매핑 (수신자 UUID 리스트)
    @JsonProperty("receiver_uuids")
    private List<String> receiverUuids;

    // 메시지 템플릿 구조를 표현하는 내부 static 클래스
    @Getter
    @Setter
    @ToString
    public static class TemplateObject {
        @JsonProperty("object_type")
        private String objectType; // 템플릿 타입(예: text)
        private String text;       // 메시지 본문
        private Link link;         // 링크 정보

        // 링크 정보를 표현하는 내부 static 클래스
        @Getter
        @Setter
        @ToString
        public static class Link {
            @JsonProperty("web_url")
            private String webUrl; // 웹 링크 URL
        }
    }

    // 생성자: uuid(수신자), text(메시지 본문)으로 요청 객체를 생성
    public KakaoTalkMessageRequest(String uuid, String text) {
        List<String> receiverUuids = Collections.singletonList(uuid); // 단일 수신자 리스트 생성

        TemplateObject.Link link = new TemplateObject.Link(); // 링크 객체 생성
        TemplateObject templateObject = new TemplateObject(); // 템플릿 객체 생성
        templateObject.setObjectType("text"); // 템플릿 타입 지정
        templateObject.setText(text);          // 메시지 본문 지정
        templateObject.setLink(link);          // 링크 정보 지정

        this.receiverUuids = receiverUuids;   // 수신자 UUID 리스트 설정
        this.templateObject = templateObject; // 템플릿 객체 설정
    }

}
