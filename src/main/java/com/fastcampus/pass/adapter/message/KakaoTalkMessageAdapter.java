package com.fastcampus.pass.adapter.message;

import com.fastcampus.pass.config.KakaoTalkMessageConfig;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Service // 비즈니스 로직을 담당하는 Service 계층의 Bean으로 등록
public class KakaoTalkMessageAdapter {
    private final WebClient webClient; // 비동기 HTTP 통신을 위한 WebClient

    // KakaoTalkMessageConfig를 주입받아 WebClient를 설정
    public KakaoTalkMessageAdapter(KakaoTalkMessageConfig config) {
        webClient = WebClient.builder()
                .baseUrl(config.getHost()) // 카카오 API 서버 주소
                .defaultHeaders(h -> {
                    h.setBearerAuth(config.getToken()); // 인증 토큰 설정
                    h.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Content-Type 설정
                }).build();
    }

    // 카카오톡 메시지 전송 메서드
    public boolean sendKakaoTalkMessage(final String uuid, final String text) {
        KakaoTalkMessageResponse response = webClient.post().uri("/v1/api/talk/friends/message/default/send")
                .body(BodyInserters.fromValue(new KakaoTalkMessageRequest(uuid, text))) // 요청 본문 생성
                .retrieve() // 요청 실행
                .bodyToMono(KakaoTalkMessageResponse.class) // 응답을 비동기적으로 변환
                .block(); // 동기적으로 결과 대기

        // 응답이 없거나 성공한 수신자 목록이 없으면 실패
        if (response == null || response.getSuccessfulReceiverUuids() == null) {
            return false;
        }
        // 한 명 이상에게 성공적으로 전송되었으면 true 반환
        return response.getSuccessfulReceiverUuids().size() > 0;
    }

}
