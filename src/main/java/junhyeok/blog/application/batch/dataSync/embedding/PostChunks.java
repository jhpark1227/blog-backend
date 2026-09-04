package junhyeok.blog.application.batch.dataSync.embedding;

import java.util.List;
import junhyeok.blog.domain.Post;
import org.springframework.ai.document.Document;

public record PostChunks(
        Post post,
        List<Document> documents
) {
}
