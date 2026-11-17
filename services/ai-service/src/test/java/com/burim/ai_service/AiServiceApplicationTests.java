package com.burim.ai_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
		"spring.ai.openai.api-key=test-key",
		"spring.ai.mcp.client.enabled=false"
})
class AiServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
