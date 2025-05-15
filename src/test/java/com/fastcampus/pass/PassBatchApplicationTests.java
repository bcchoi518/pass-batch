// 이 클래스는 Spring Boot 테스트가 정상적으로 동작하는지 확인하는 기본 테스트 클래스입니다.
package com.fastcampus.pass;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest // Spring Boot 환경에서 테스트를 실행합니다.
class PassBatchApplicationTests {

	@Test // 이 메서드가 테스트임을 나타냅니다.
	void contextLoads() {
		// 아무 동작도 하지 않지만, 애플리케이션 컨텍스트가 정상적으로 로드되는지 확인합니다.
	}

}
