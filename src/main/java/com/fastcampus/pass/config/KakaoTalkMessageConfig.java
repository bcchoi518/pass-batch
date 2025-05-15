package com.fastcampus.pass.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter // Lombok: getter 메서드 자동 생성
@Setter // Lombok: setter 메서드 자동 생성
@Component // Spring Bean으로 등록
@ConfigurationProperties(prefix = "kakaotalk") // application.yml의 kakaotalk.* 값을 자동으로 바인딩
public class KakaoTalkMessageConfig {
    private String host;  // 카카오 API 서버 주소
    private String token; // 카카오 API 인증 토큰
}
