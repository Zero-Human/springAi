package com.example.springai.chapter2;

import com.example.springai.summary.DateTools;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@RestController
public class ModelClient {
    private final ChatModel chatModel;

    @GetMapping("/v1/model-client")
    public ChatResponse modelClientV1(@RequestParam("text") String text){
        // 동기식 및 스트리밍 프로그래밍 모델을 모두 지원합니다
        // 다양한 adviser, tool 등을 이용할 수 있는 상위 방식
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해."),
                        new UserMessage( text )
                ));

        ChatResponse response = ChatClient.create(chatModel)
                .prompt(prompt)
                .call()
                .chatResponse();
        return response;
    }

    @GetMapping("/v2/model-client")
    public ActorFilms modelClientV2(@RequestParam("text") String text){
        // String으로 결과 받기
        // qwen은 추론과정도 보여주기 때문에 계속 오류가 발생한다.
        //.content()가 String이고 entity는 객체타입으로 맵핑하는 것인데 잘 안되네;
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해"),
                        new UserMessage( text )
                ));

        ChatModel gemma = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().build())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("gemma3:1b")
                                .temperature(0.9)
                                .build())
                .build();

        ActorFilms response = ChatClient.create(gemma)
                .prompt(prompt)
                .call()
                .entity(ActorFilms.class);
        return response;
    }

    @GetMapping(value ="/v3/model-client", produces = "text/plain;charset=UTF-8")
    public Flux<String> getModelClientV3(@RequestParam("text") String text){
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
}

// 불변(immutable) 데이터 캐리어 클래스 JAVA 16 이상
record ActorFilms(String actor, List<String> movies) {}