package junhyeok.blog.infrastructure;

import java.util.Set;
import junhyeok.blog.application.ContentExtractor;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
public class NotionContentExtractor implements ContentExtractor {

    private static final Set<String> SKIP_TYPES = Set.of(
            "page", "collection_view_page", "collection_view", "image"
    );
    private static final String INLINE_PLACEHOLDER = "‣";

    private final ObjectMapper objectMapper;

    public NotionContentExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String extractPlainText(String content) {
        try {
            JsonNode blockMap = objectMapper.readTree(content).path("block");

            StringBuilder sb = new StringBuilder();
            for (String blockId : blockMap.propertyNames()) {
                JsonNode value = blockMap.path(blockId).path("value");
                if (SKIP_TYPES.contains(value.path("type").asString())) {
                    continue;
                }

                String blockText = extractTitle(value.path("properties").path("title"));
                if (!blockText.isBlank()) {
                    sb.append(blockText).append("\n");
                }
            }

            return sb.toString().strip();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.NOTION_RESPONSE_INVALID);
        }
    }

    private String extractTitle(JsonNode title) {
        if (!title.isArray()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (JsonNode run : title) {
            if (!run.isArray() || run.isEmpty()) {
                continue;
            }
            String text = run.get(0).asString();
            if (!INLINE_PLACEHOLDER.equals(text)) {
                sb.append(text);
            }
        }
        return sb.toString();
    }
}
