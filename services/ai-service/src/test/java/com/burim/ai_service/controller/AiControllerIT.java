package com.burim.ai_service.controller;

import com.burim.ai_service.dto.ChatRequest;
import com.burim.ai_service.dto.ChatResponse;
import com.burim.ai_service.service.AiChatService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.ResourceAccessException;

import java.net.http.HttpTimeoutException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.ai.openai.api-key=test-key",
        "spring.ai.mcp.client.enabled=false"
})
@AutoConfigureMockMvc
class AiControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AiChatService aiChatService;

    @Test
    @DisplayName("Health check публичный")
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("chat: без JWT возвращает 401")
    void chatRequiresJwt() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Найди клавиатуру\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("chat: пустое сообщение возвращает 400 VALIDATION_FAILED")
    void chatReturns400WhenMessageBlank() throws Exception {
        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("chat: сообщение более 2000 символов возвращает 400 VALIDATION_FAILED")
    void chatReturns400WhenMessageTooLong() throws Exception {
        String longMessage = "a".repeat(2001);
        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"message\":\"%s\"}", longMessage)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("chat: валидный запрос возвращает 200 и ответ модели")
    void chatReturnsOkWithValidMessage() throws Exception {
        when(aiChatService.chat(any(ChatRequest.class)))
                .thenReturn(new ChatResponse("Я нашел клавиатуру Keychron K2"));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Найди клавиатуру до 150 долларов\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("Я нашел клавиатуру Keychron K2"));
    }

    @Test
    @DisplayName("chat: при недоступности зависимостей возвращает 503 AI_DEPENDENCY_UNAVAILABLE")
    void chatReturns503WhenDependencyUnavailable() throws Exception {
        when(aiChatService.chat(any(ChatRequest.class)))
                .thenThrow(new ResourceAccessException("Connection refused: mcp-server"));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Найди клавиатуру\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.error").value("AI_DEPENDENCY_UNAVAILABLE"));
    }

    @Test
    @DisplayName("chat: при таймауте возвращает 504 AI_TIMEOUT")
    void chatReturns504WhenTimeout() throws Exception {
        when(aiChatService.chat(any(ChatRequest.class)))
                .thenThrow(new RuntimeException(new HttpTimeoutException("request timed out")));

        mockMvc.perform(post("/api/v1/ai/chat")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"message\":\"Найди клавиатуру\"}"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(jsonPath("$.status").value(504))
                .andExpect(jsonPath("$.error").value("AI_TIMEOUT"));
    }
}
