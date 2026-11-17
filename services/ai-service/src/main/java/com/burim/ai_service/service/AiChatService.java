package com.burim.ai_service.service;

import com.burim.ai_service.dto.ChatRequest;
import com.burim.ai_service.dto.ChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatService {

    private final ChatClient chatClient;

    public ChatResponse chat(ChatRequest request) {
        log.info("Processing AI chat request: length={}", request.message().length());
        long start = System.currentTimeMillis();

        String answer = chatClient.prompt()
                .user(request.message())
                .call()
                .content();

        long duration = System.currentTimeMillis() - start;
        log.info("AI chat request completed successfully in {} ms, answer length={}", duration, (answer != null ? answer.length() : 0));

        return new ChatResponse(answer);
    }
}
