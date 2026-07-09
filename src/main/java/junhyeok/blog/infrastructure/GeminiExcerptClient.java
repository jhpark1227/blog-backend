package junhyeok.blog.infrastructure;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import junhyeok.blog.application.ExcerptClient;
import junhyeok.blog.domain.Excerpt;
import junhyeok.blog.domain.Post;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class GeminiExcerptClient implements ExcerptClient {

    private static final String PROMPT_TEMPLATE = """
            다음 블로그 글을 한국어로 요약해서, 글 목록에 보여줄 요약문(excerpt)을 작성해줘.
            
            규칙:
            - 120자 이내의 1~2문장으로 작성한다.
            - 글의 핵심 주제만 담는다.
            - 인사말, 따옴표, "요약:" 같은 접두어 없이 요약문 텍스트만 출력한다.
            
            글 내용:
            %s
            """;

    private final RestClient restClient;
    private final NotionContentExtractor contentExtractor;

    public GeminiExcerptClient(
            @Value("${gemini.api-key}") String geminiApiKey,
            NotionContentExtractor contentExtractor
    ) {
        this.restClient = RestClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com")
                .defaultHeader("x-goog-api-key", geminiApiKey)
                .build();
        this.contentExtractor = contentExtractor;
    }

    @Override
    public Excerpt generateExcerpt(Post post) {
        String plainText = contentExtractor.extractPlainText(post.getContent());
        String prompt = PROMPT_TEMPLATE.formatted(plainText);
        GeminiResponse responseBody = restClient.post()
                .uri("/v1/interactions")
                .body(Map.of("model", "gemini-3.5-flash",
                        "input", prompt))
                .retrieve()
                .body(GeminiResponse.class);

        if (responseBody == null || responseBody.steps() == null) {
            throw new CustomException(ErrorCode.GEMINI_RESPONSE_INVALID);
        }

        return new Excerpt(extractText(responseBody));
    }

    private String extractText(GeminiResponse responseBody) {
        return responseBody.steps().stream()
                .filter(step -> "model_output".equals(step.type()))
                .findFirst()
                .map(Step::text)
                .orElseThrow(() -> new CustomException(ErrorCode.GEMINI_RESPONSE_INVALID));
    }

    private record GeminiResponse(
            List<Step> steps
    ) {
    }

    private record Step(
            String type,
            List<Content> content
    ) {
        private String text() {
            if (content == null) {
                return "";
            }
            return content.stream()
                    .map(Content::text)
                    .collect(Collectors.joining());
        }
    }

    private record Content(
            String text
    ) {
    }
}
