package com.example.springai.summary;

import lombok.AllArgsConstructor;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.document.Document;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@AllArgsConstructor
@RestController
public class Controller {

    private final ServiceV1 serviceV1;

    @GetMapping("/summary/{model}")
    public String summary(@RequestParam String text, @PathVariable("model") String model){
        System.out.println("start");
        String result =  serviceV1.summary(text,model);
        System.out.println("end");
        return result;

    }
    @PostMapping("/summary/{model}")
    public String summary2(@RequestBody String text, @PathVariable("model") String model){
        System.out.println("start");
        String result =  serviceV1.summary(text, model);
        System.out.println("end");
        return result;

    }
    @PostMapping("/summary/V2/{model}")
    public String summaryV2(@RequestBody String text, @PathVariable("model") String model){
        String result =  serviceV1.summaryV2(text, model);
        return result;

    }
    @GetMapping("/tools")
    public String summary() throws Exception{
        return serviceV1.useTool();
    }
    @GetMapping("/embedding")
    public Object embedding(){
        return serviceV1.embedding();
    }

    @GetMapping("/vectorStore/{text}")
    public List<Document> SearchvectorStore(@PathVariable("text") String text){
        return serviceV1.searchVectorStore(text);
    }

    @PostMapping("/vectorStore")
    public String vectorStore(@RequestBody String text){
        serviceV1.addVectorStore(text);
        return "성공";
    }

    @PostMapping("/vectorStore/ai")
    public List<Generation> SearchvectorStoreAndAi(@RequestBody String text){
        return serviceV1.searchVectorStoreAndAi(text);
    }
    @PostMapping("/vectorStore/ai/v2")
    public String SearchvectorStoreAndAiV2(@RequestBody String text){
        return serviceV1.searchVectorStoreAndAiV2(text);
    }
}
