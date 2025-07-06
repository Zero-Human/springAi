package com.example.springai.chapter1;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
public class ChatModel {
    private final OllamaChatModel chatModel;

    @GetMapping("/v1/chat-model")
    public String chatMedol(@RequestParam("text") String text){
        // 가장 간단하게 입력한 내용을 그대로 llm에게 전달하여 받는 방식이다.
        // llm을 저수준 단위에서 사용하는 방법
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 요약 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해."),
                        new UserMessage("다음 내용 한국어로 요약해.:\n" + text )
                ));
        ChatResponse response = this.chatModel.call(prompt);
        System.out.println(response);
        return response.getResult().getOutput().getText();
    }
    @GetMapping("/v2/chat-model")
    public String chatMedolV2(@RequestParam("text") String text, @RequestParam("model") String model){
        // 프롬프트를 탬플릿화하여 메시지를 입력하고 그것을 결과로 받는 방식
        // 모델을 직접 입력받아서 사용할 수 있다. ollama로 다운받지 않은 모델을 입력 시 없다고 오류 반환한다
        Prompt prompt = new Prompt(
                List.of(
                        this.makeSystemMessage("라면땅아"),
                        this.makeUserMessage(text)
                ),
                OllamaOptions.builder()
                .model(model)
                .build());
        ChatResponse response = this.chatModel.call(prompt);
        System.out.println(response);
        return response.getResult().getOutput().getText();
    }

    public Message makeSystemMessage(String name){
        String systemMessage = "너는 친절한 AI 요약비서야 너는 항상 대답할 때 {name}을 먼저 이야기하고 내용을 이야기해";
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemMessage);
        return systemPromptTemplate.createMessage(Map.of("name",name));
    }

    public Message makeUserMessage(String text){
        // 기본적으로 {}이 치환하는 값이지만 json타입일 경우 {}를 많이 사용하다보니
        // 다른 토큰으로 도 사용할 수 있다는 것을 보여줌
        String UserMessage = "다음 내용 한국어로 요약해.<text>";
        PromptTemplate userPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template(UserMessage)
                .build();
        return userPromptTemplate.createMessage(Map.of("text",text));
    }

}
