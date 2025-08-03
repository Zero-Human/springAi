package com.example.springai.chapter3;

import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.messages.UserMessage;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

public class ReReadingAdvisor implements CallAdvisor, StreamAdvisor {

    private int order = 1;
    public ReReadingAdvisor(){
    }
    public ReReadingAdvisor(int order){
        this.order = order;
    }

    private ChatClientRequest before(ChatClientRequest advisedRequest) {
        // 추론 능력을 향상시키는 다시 읽기(Re-Reading, Re2)라는 기법을 적용(Spring ai에서 예시로 소개함)
        // 입력한 사용자 프롬프트를 가져온다.
        String userText = advisedRequest.prompt().getUserMessage().getText();
        // 프롬프트에 추가적인 내용을 적어서 전달
        String augmented = userText + "\nRead the question again: " + userText;

        return advisedRequest.builder()
                .prompt(advisedRequest.prompt().augmentUserMessage(augmented))
                .context(advisedRequest.context())
                .build();
    }
    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        // 동기식일 경우 사용되는 함수
        ChatClientRequest newChatClientRequest = this.before(chatClientRequest);
        ChatClientResponse chatClientResponse =callAdvisorChain.nextCall(newChatClientRequest);
        return chatClientResponse;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        // 비동기식일 경우 사용되는 함수
        return streamAdvisorChain.nextStream(this.before(chatClientRequest));
    }

    @Override
    public String getName() {
        // advisor 이름
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        // 실행되는 우선 순위
        return this.order;
    }
}
