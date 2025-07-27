package com.example.springai.chapter2;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
public class ModelClient {
    private final ChatModel chatModel;

    @GetMapping("/v1/model-client")
    public String modelClientV1(@RequestParam("text") String text){
        // 한번에 출력하는 방식으로 전달하는 법
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해."),
                        new UserMessage( text )
                ));

        return ChatClient.create(chatModel)
                .prompt(prompt)
                .call()
                .content();
    }

    @GetMapping(value ="/v2/model-client", produces = "text/plain;charset=UTF-8")
    public Flux<String> getModelClientV2(@RequestParam("text") String text){
        // 스트리밍 방식으로 전달하는 법
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 비서야"),
                        new UserMessage( text )
                ));
        return ChatClient.create(this.chatModel)
                .prompt(prompt)
                .stream()
                .content();
    }

    @GetMapping("/v3/model-client")
    public ActorFilms modelClientV3(@RequestParam("text") String text){
        // 구조화된 출력하기
        String template = """
        Generate the filmography of 5 movies for {actor}.
        """;

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해"),
                        new UserMessage(new PromptTemplate(template).render( Map.of("actor", text)))
                ));

        ChatModel gemma = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().build())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("gemma3n:e4b")
                                .temperature(0.4)
                                .build())

                .build();

        ActorFilms response = ChatClient.create(gemma)
                .prompt(prompt)
                .advisors(new SimpleLoggerAdvisor(10))
                .call()
                .entity(ActorFilms.class);
        return response;
    }



}

// 불변(immutable) 데이터 캐리어 클래스 JAVA 16 이상
record ActorFilms(String actor, List<String> movies) {}