package com.example.springai.ex1;


import lombok.AllArgsConstructor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher;
import org.springframework.ai.model.transformer.SummaryMetadataEnricher.SummaryType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@AllArgsConstructor
public class summary {

    private final ChatModel chatModel;

    @PostMapping("/v1/summary")
    public List<Document> addRag(@RequestBody String text){
        // 요약 기능 함수
        SummaryMetadataEnricher summaryMetadataEnricher = new SummaryMetadataEnricher(
                                                    chatModel,
                List.of(SummaryType.PREVIOUS, SummaryType.CURRENT, SummaryType.NEXT),
                """
                        다음은 섹션의 내용입니다:
                        {context_str}
                                                
                        섹션의 주요 주제와 엔티티를 요약합니다.
                                                
                        요약:
                        """,
                MetadataMode.ALL
        );

        List<Document> enrichedDocs = summaryMetadataEnricher.apply(List.of(new Document(text)));
        return enrichedDocs;
    }
}
