package com.example.springai.chapter5;

import com.example.springai.chapter3.ReReadingAdvisor;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
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
public class Tools {

    private final ChatModel chatModel;

    @GetMapping("/v1/tools")
    public ChatResponse tools(@RequestParam("text") String text){


        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 도구를 유용하게 잘 사용하는 친절한 AI 비서이야"),
                        new UserMessage( text )
                ));
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultTools(new DateTools())
                .build();

        ChatResponse response = chatClient
                .prompt(prompt)
                .call()
                .chatResponse();
        return response;
    }
}
