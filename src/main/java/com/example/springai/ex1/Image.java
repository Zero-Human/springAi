package com.example.springai.ex1;


import lombok.AllArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.chroma.vectorstore.ChromaVectorStore;
import org.springframework.ai.content.Media;
import org.springframework.ai.document.Document;
import org.springframework.ai.image.ImageModel;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.ollama.api.OllamaApi;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
@AllArgsConstructor
public class Image {
    private final ChatModel chatModel;

    private final ChromaVectorStore vectorStore;

    @PostMapping("/v1/image/{model2}")
    public Flux<String> imageV1(@RequestBody MultipartFile multipartFile, @PathVariable String model2){

        var imageResource = new ClassPathResource("/multimodal.test.png");
        String system = """
                You are an expert CSS developer
                You take screenshots of a reference web page from the user, and then build single page apps\s
                using CSS, HTML and JS.
                                
                - Make sure the app looks exactly like the screenshot.
                - Pay close attention to background color, text color, font size, font family,\s
                padding, margin, border, etc. Match the colors and sizes exactly.
                - Use the exact text from the screenshot.
                - Do not add comments in the code such as "<!-- Add other navigation links as needed -->" and "<!-- ... other news items ... -->" in place of writing the full code. WRITE THE FULL CODE.
                - Repeat elements as needed to match the screenshot. For example, if there are 15 items, the code should have 15 items. DO NOT LEAVE comments like "<!-- Repeat for each news item -->" or bad things will happen.
                - For images, use placeholder images from https://placehold.co and include a detailed description of the image in the alt text so that an image generation AI can generate the image later.
                                
                In terms of libraries,
                                
                - You can use Google Fonts
                - Font Awesome for icons: <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.3/css/all.min.css"></link>
                                
                Return only the full code in <html></html> tags.
                Do not include markdown "```" or "```html" at the start or end.
                """;
        var userMessage = UserMessage.builder()
                .text("You are an expert front-end developer. Given the attached screenshot, generate a complete, semantic HTML5 page with matching CSS styles to replicate the design as closely as possible.\n" +
                        "\n" +
                        "Requirements:\n" +
                        "- Use only standard HTML5 and CSS (no frameworks).\n" +
                        "- Use semantic HTML tags: `<header>`, `<nav>`, `<main>`, `<section>`, `<footer>`, etc.\n" +
                        "- Write clean, maintainable CSS either in a `<style>` tag in the `<head>` or an external `.css` file.\n" +
                        "- Make the layout responsive: on screens narrower than 600px, stack layouts vertically using media queries.\n" +
                        "- Match the screenshot’s colors, fonts, spacing, alignment, and element proportions.\n" +
                        "- Ensure accessibility: all images include `alt` attributes; use ARIA roles or labels for interactive elements like buttons or nav.\n" +
                        "- Provide only the complete code block, with `<html> … </html>`; no extra commentary or explanation.\n")
                .media(List.of(new Media(MimeTypeUtils.IMAGE_PNG, multipartFile.getResource())))
                .build();

        ChatModel gemma = OllamaChatModel.builder()
                .ollamaApi(OllamaApi.builder().baseUrl("https://5ce215bb659c.ngrok-free.app").build())
                .defaultOptions(
                        OllamaOptions.builder()
                                .model(model2)
                                .keepAlive("30m")
                                .build())
                .build();

        ChatClient chatClient = ChatClient.builder(gemma)
                .build();


        return chatClient.prompt(new Prompt(List.of(userMessage, new SystemMessage(system)))).advisors(new SimpleLoggerAdvisor(10)).stream().content();
    }

}
