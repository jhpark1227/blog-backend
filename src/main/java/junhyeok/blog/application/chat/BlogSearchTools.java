package junhyeok.blog.application.chat;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BlogSearchTools {

    private final VectorStore vectorStore;

    public BlogSearchTools(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Tool(
            name = "search_blog_posts",
            description = """
                    Semantic (vector) search over the full text of this blog's published posts.
                    Use for ANY question about what a post says, explains, or concludes.
                    Each result starts with a '# title' header line, so cite posts by that title.
                    An empty result means the blog has no relevant post - say so instead of guessing.
                    """
    )
    public List<SearchedChunk> searchBlogPosts(
            @ToolParam(description = """
                    Self-contained Korean search query. Resolve pronouns from the conversation first.
                    e.g. '스프링 배치 청크 지향 처리'""") String query
    ) {
        List<Document> documents = vectorStore.similaritySearch(SearchRequest.builder()
                .query(query)
                .topK(5)
                .similarityThreshold(0.5)
                .build());

        if (documents == null || documents.isEmpty()) {
            log.info("[BLOG_SEARCH_EMPTY] query={}", query);
            return List.of();
        }

        log.info("[BLOG_SEARCH] query={}, hits={}", query, documents.size());
        return documents.stream()
                .map(document -> new SearchedChunk(
                        String.valueOf(document.getMetadata().get("postId")),
                        document.getText()))
                .toList();
    }

    public record SearchedChunk(
            String postId,
            String content
    ) {
    }
}