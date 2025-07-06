package com.example.springai.chapter6;

import com.example.springai.chapter3.ReReadingAdvisor;
import com.example.springai.chapter5.DateTools;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class Rag {

    private final ChatModel chatModel;

    private final ChromaVectorStore vectorStore;



    @PostMapping("/v1/rag")
    public String addRag(@RequestBody String text){
        // 따로 vectorStore를 만들지 않으면 기본 설정된 embedding 모델이 들어간다.
        List<Document> documents = List.of(new Document(text));
        vectorStore.add(documents);
        return "성공";
    }

    @GetMapping("/v1/rag")
    public String findRag(@RequestBody String text){

        PromptTemplate customPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
            <query>

            맥락 정보는 아래와 같습니다.

			---------------------
			<question_answer_context>
			---------------------

			맥락 정보에 주어진 내용으로만 대답해

			다음 규칙을 따르십시오:

			1. 만약 문맥상 답이 명확하지 않다면, 그냥 모른다고 말하세요.
			2. "맥락에 따라..." 또는 "제공된 정보에 따르면..."과 같은 표현은 피하세요.
			3. 제공된 맥락 정보도 같이 알려줘
            """)
                .build();
        // similarityThreshold()는 유사도
        // Rag하기 위해 백터DB 셋팅하기
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(5).build())
                .promptTemplate(customPromptTemplate)
                .build();
        ChatClient chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(questionAnswerAdvisor, new SimpleLoggerAdvisor(),new ReReadingAdvisor())
                .build();
        return chatClient.prompt(text)
                .call()
                .content();
    }
}
