package com.example.springai.summary;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ServiceV1 {

    private final OllamaChatModel chatModel;

    public String summary(String text){
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 요약 비서야. 절대 추론과정 없이 요약만 응답해."),
                        new UserMessage("다음 내용 한국어로 요약해. 추론 과정 없이 최종 요약만 출력해:\n" + text)
                ),
                OllamaOptions.builder()
                        .model("qwen3:1.7b-q4_K_M")
                        .build());

        ChatResponse response = this.chatModel.call(prompt);
        System.out.println(response);
        return response.getResult().getOutput().getText();
    }
}
