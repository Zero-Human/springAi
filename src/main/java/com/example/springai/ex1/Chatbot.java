package com.example.springai.ex1;


import com.example.springai.chapter3.ReReadingAdvisor;
import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@AllArgsConstructor
public class Chatbot {
    private final ChatModel chatModel;

    private final ChromaVectorStore vectorStore;

    @PostMapping("/v1/add-file")
    public String addFile(@RequestBody MultipartFile multipartFile){
        // 백터 db에 파일 넣기
        List<Document> docs = new TikaDocumentReader(multipartFile.getResource()).get();
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(400)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(5000)
                .withKeepSeparator(true)
                .build();
        List<Document> chunks = splitter.apply(docs);
        vectorStore.add(chunks);
        return "성공";
    }

    @PostMapping("/v2/add-file")
    public String addFile2(@RequestBody String text){
        // 백터 db에 파일 넣기
        List<Document> documents = List.of(new Document(text));
        TokenTextSplitter splitter = TokenTextSplitter.builder()
                .withChunkSize(400)
                .withMinChunkSizeChars(200)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(5000)
                .withKeepSeparator(true)
                .build();
        var chunks = splitter.apply(documents);
        vectorStore.add(chunks);
        return "성공";
    }

    @GetMapping("/v1/chat-bot")
    public String chatBotV1(@RequestBody String text){

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("당신은 친절하고 신뢰할 수 있는 한국어 QA 챗봇입니다.\n" +
                                "답변을 제공하기 전에 스스로 “이 답변이 문서(또는 외부 지식)에 기반한 사실인지” 검증하세요.\n" +
                                "모르는 내용은 “모르겠습니다”라고 답하고, 근거가 불충분하면 추가 확인을 요청하세요."),
                        new UserMessage( text )
                ));

        PromptTemplate customPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
            <query>

            맥락 정보는 아래와 같습니다.

			---------------------
			<question_answer_context>
			---------------------

			위 문서를 참고하여 답변하세요. 사실 근거를 구체적으로 언급하고, 근거 없으면 “모르겠습니다”라고 답하세요.
            """)
                .build();
        // similarityThreshold()는 유사도

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(5).similarityThreshold(0.3d).build())
                .promptTemplate(customPromptTemplate)
                .build();

        ChatModel gemma = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl("").build())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("qwen3:4b")
                                .temperature(0.5)
                                .build())
                .build();

        ChatClient chatClient = ChatClient.builder(gemma)
                .build();


        String res = chatClient.prompt(prompt)
                .advisors(questionAnswerAdvisor)
                .call()
                .content();
        System.out.println("res:"+res);

        String draft = chatClient.prompt(prompt.augmentUserMessage("단계별 사고 과정을 통해 답변 초안을 작성하세요.\n\"" + res + "\""))
                .call()
                .content();

        System.out.println("deaft:"+draft);
        String questions = chatClient.prompt(prompt.augmentUserMessage("답변의 주장 중 사실관계 확인이 필요한 부분을 *검증 질문* 형태로 3–5개 만들어주세요.:\n\"" + draft + "\""))
                        .call()
                        .content();

        System.out.println("questions:"+questions);
        String answers = chatClient.prompt(prompt.augmentUserMessage("질문에 대해 문서 기반으로 답해주세요. 답이 있으면 명시적으로 밝히고, 없으면 “정보 부족으로 확인 불가”라고 답하세요.:\n" + questions))
                        .advisors(questionAnswerAdvisor)
                        .call()
                        .content();

        System.out.println("answers:"+answers);
        return chatClient.prompt(prompt.augmentUserMessage("""
                초안:
                %s
                
                검증 답변:
                %s
                
                위 검증 결과를 바탕으로, 오타나 잘못된 정보가 있다면 수정하여 최종 정제된 답변을 한국어로 작성하세요.
               """.formatted(draft, answers)))
                .call()
                .content();
    }

    @GetMapping("/v2/chat-bot")
    public String chatBotV2(@RequestBody String text){

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("당신은 친절하고 신뢰할 수 있는 한국어 QA 챗봇입니다.\n" +
                                "답변을 제공하기 전에 스스로 “이 답변이 문서(또는 외부 지식)에 기반한 사실인지” 검증하세요.\n" +
                                "모르는 내용은 “모르겠습니다”라고 답하고, 근거가 불충분하면 추가 확인을 요청하세요."),
                        new UserMessage( text )
                ));

        PromptTemplate customPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
            <query>

            맥락 정보는 아래와 같습니다.

			---------------------
			<question_answer_context>
			---------------------

			위 문서를 참고하여 답변하세요. 사실 근거를 구체적으로 언급하고, 근거 없으면 “모르겠습니다”라고 답하세요.
            """)
                .build();
        // similarityThreshold()는 유사도

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(5).similarityThreshold(0.3d).build())
                .promptTemplate(customPromptTemplate)
                .build();

        ChatModel model = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl("").build())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model("qwen3:4b")
                                .temperature(0.5)
                                .build())
                .build();

        ChatClient chatClient = ChatClient.builder(model)
                .build();

        return chatClient.prompt(prompt)
                .advisors(questionAnswerAdvisor,new SimpleLoggerAdvisor())
                .call()
                .content();
    }
    @GetMapping("/v3/chat-bot/{model2}")
    public String chatBotV3(@RequestBody String text, @PathVariable String model2){

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("당신은 친절하고 신뢰할 수 있는 한국어 QA 챗봇입니다.\n" +
                                "제공한 정보를 참고하여 알려줘.\n"),
                        new UserMessage( text )
                ));

        PromptTemplate customPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
            <query>

            맥락 정보는 아래와 같습니다.

			---------------------
			<question_answer_context>
			---------------------

            """)
                .build();
        // similarityThreshold()는 유사도

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(5).similarityThreshold(0.3d).build())
                .promptTemplate(customPromptTemplate)
                .build();

        ChatModel model = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl("https://ef8b99cd60f5.ngrok-free.app").build())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model(model2)
                                .temperature(0.5)
                                .build())
                .build();

        ChatClient chatClient = ChatClient.builder(model)
                .build();

        return chatClient.prompt(prompt)
                .advisors(questionAnswerAdvisor,new SimpleLoggerAdvisor(10))
                .call()
                .content();
    }

    @GetMapping("/v4/chat-bot/{model2}")
    public String chatBotV4(@RequestBody String text, @PathVariable String model2){

        Prompt prompt = new Prompt(
                List.of(
                        new SystemMessage("당신은 친절하고 신뢰할 수 있는 코딩 어시스턴스 챗봇입니다.\n"),
                        new UserMessage( text )
                ));

        PromptTemplate customPromptTemplate = PromptTemplate.builder()
                .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                .template("""
            <query>

            맥락 정보는 아래와 같습니다.

			---------------------
			<question_answer_context>
			---------------------

            """)
                .build();
        // similarityThreshold()는 유사도

        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder().topK(5).similarityThreshold(0.3d).build())
                .promptTemplate(customPromptTemplate)
                .build();

        ChatModel model = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl("").build())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model(model2)
                                .temperature(0.5)
                                .build())
                .build();

        ChatClient chatClient = ChatClient.builder(model)
                .build();

        return chatClient.prompt(prompt)
                .advisors(new SimpleLoggerAdvisor(10))
                .call()
                .content();
    }
}
