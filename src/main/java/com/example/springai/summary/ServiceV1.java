package com.example.springai.summary;


import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingOptions;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.OllamaEmbeddingModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class ServiceV1 {

    private final OllamaChatModel chatModel;
    private final EmbeddingModel embeddingModel;

    private final VectorStore vectorStore;

    public String summary(String text,String model){
        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("너는 친절한 AI 요약 비서야. 절대 추론과정을 보여주지 말고 요약만 응답해."),
                        new UserMessage("다음 내용 한국어로 요약해.:\n" + text )
                ),
                OllamaOptions.builder()
                        .model(model)
                        .build());

        ChatResponse response = this.chatModel.call(prompt);
        System.out.println(response);
        return response.getResult().getOutput().getText();
    }

    public String summaryV2(String text,String model){
        Prompt prompt = new Prompt(
                List.of(
                        this.makeSystemMessage(model),
                        this.makeUserMessage(text)
                ),
                OllamaOptions.builder()
                        .model(model)
                        .build());
        System.out.println("getSystemMessage: "+prompt.getSystemMessage().getText());
        System.out.println("getUserMessage: "+prompt.getUserMessage());
        ChatResponse response = this.chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    public Prompt makePrompt(String text){
        PromptTemplate promptTemplate = new PromptTemplate("다음 내용 한국어로 요약해.:\n{context}");
        Prompt prompt = promptTemplate.create(Map.of("context", text));
        return prompt;
    }

    public Message makeSystemMessage(String name){
        String systemMessage = "너는 친절한 AI 요약비서야 너는 항상 대답할 때 {name}을 먼저 이야기하고 내용을 이야기해";
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemMessage);
        return systemPromptTemplate.createMessage(Map.of("name",name));
    }

    public Message makeUserMessage(String text){
        String UserMessage = "다음 내용 한국어로 요약해.{text}";
        PromptTemplate userPromptTemplate = new PromptTemplate(UserMessage);
        return userPromptTemplate.createMessage(Map.of("text",text));
    }

    public String useTool(){
        System.out.println(this.chatModel.getDefaultOptions().getModel());
        String response = ChatClient.create(chatModel)
                .prompt("현재 시간을 확인하고 10분 후에 알람을 설정해줘")
                .tools(new DateTools())
                .call()
                .content();
        return response;
    }

    public String useToolForChatModel(){

        ToolCallback[] dateTimeTools = ToolCallbacks.from(new DateTools());
        Prompt prompt = new Prompt("현재 시간을 확인하고 10분 후에 알람을 설정해줘",
                OllamaOptions.builder()
                        .model("qwen3:1.7b")
                        .toolCallbacks(dateTimeTools)
                        .build());
        System.out.println(prompt.getOptions().getModel());
        ChatResponse response = this.chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }

    public EmbeddingResponse embedding(){

        EmbeddingOptions embeddingOptions = OllamaOptions.builder()
                .model("bona/bge-m3-korean")
                        .truncate(false)
                .build();
        EmbeddingResponse embeddingResponse = embeddingModel.call(
                new EmbeddingRequest(List.of("Hello World", "World is big and salvation is near"),
                        embeddingOptions));
        System.out.println(embeddingResponse);
        return  embeddingResponse;
    }

    public void addVectorStore(String text){
        List<Document> documents = List.of(new Document(text));
        vectorStore.add(documents);
    }
    public List<Document> searchVectorStore(String text){
        List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query(text).topK(5).build());
        return results;
    }

    public List<Generation> searchVectorStoreAndAi(String text){
        ChatResponse response = ChatClient.builder(chatModel)
                .build().prompt()
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .user(text)
                .call()
                .chatResponse();
        return response.getResults();
    }

    public String searchVectorStoreAndAiV2(String text){

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

        System.out.println(customPromptTemplate.getTemplate());

        String question = text;

        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .promptTemplate(customPromptTemplate)
                .build();

        String response = ChatClient.builder(chatModel).build()
                .prompt(question)
                .advisors(qaAdvisor)
                .call()
                .content();


        return response;

    }


    public List<Document> vectorStore(){
        List <Document> documents = List.of(
                new Document("Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!! Spring AI rocks!!", Map.of("meta1", "meta1")),
                new Document("The World is Big and Salvation Lurks Around the Corner"),
                new Document("You walk forward facing the past and you turn back toward the future.", Map.of("meta2", "meta2")));
        System.out.println(vectorStore);

// Add the documents
        vectorStore.add(documents);

// Retrieve documents similar to a query
        List<Document> results = this.vectorStore.similaritySearch(SearchRequest.builder().query("Spring").topK(5).build());
        return results;
    }

}
