package com.example.springai.chapter4;

import com.example.springai.chapter3.ReReadingAdvisor;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
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
public class ChatMemoryEx {

    private final ChatModel chatModel;
    private final ChatMemory chatMemory;

    @GetMapping("/v1/chat-memory")
    public ChatResponse chatMemoryV1(@RequestParam("text") String text){
        // chatMemory(MessageWindowChatMemory - 구현체)는 기본값이 최근 20개의 대화를 기록하고 conversationId마다 저장한다.
        // RDB, 에도 저장 가능하다
        // PostgreSQL, MySQL / MariaDB, SQL Server, HSQLDB

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해."),
                        new UserMessage( text )
                ));
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
                .build();

        ChatResponse response = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();
        return response;
    }
    @GetMapping("/v2/chat-memory")
    public ChatResponse chatMemoryV2(@RequestParam("text") String text){
        // PromptChatMemoryAdvisor(인메모리)는 기본값이 최근 20개의 대화를 기록하고 conversationId마다 저장한다.
        // PromptChatMemoryAdvisor 시스템 프롬프트에 기존에 요청과 대답을 추가하여 요청한다

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해."),
                        new UserMessage( text )
                ));
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(PromptChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
                .build();

        ChatResponse response = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();
        return response;
    }
}
