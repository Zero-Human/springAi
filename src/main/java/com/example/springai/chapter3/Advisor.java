package com.example.springai.chapter3;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@AllArgsConstructor
@RestController
public class Advisor {
    private final ChatModel chatModel;

    @GetMapping("/v1/advisor")
    public ChatResponse advisor(@RequestParam("text") String text){
        // llm에게 부차적인 정보나 기능들을 제공할 수 있는 방법이다.
        // 가장 높은 우선순위(가장 낮은 순서 값)를 가진 advisor 스택의 맨 위에 추가되어 가장 먼저 시작함
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해."),
                        new UserMessage( text )
                ));
        ChatClient chatClient = ChatClient.builder(chatModel)
                                            .defaultAdvisors(new ReReadingAdvisor())
                                        .build();

        ChatResponse response = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();
        return response;
    }
}
