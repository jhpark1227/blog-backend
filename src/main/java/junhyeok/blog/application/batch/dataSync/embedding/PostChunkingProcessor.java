package junhyeok.blog.application.batch.dataSync.embedding;

import java.util.List;
import java.util.stream.Collectors;
import junhyeok.blog.application.ContentExtractor;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostChunkingProcessor implements ItemProcessor<Post, PostChunks> {

    private static final int CHUNK_SIZE = 500;
    private static final int MIN_CHUNK_LENGTH_TO_EMBED = 100;

    private final ContentExtractor contentExtractor;
    private final TokenTextSplitter tokenTextSplitter = TokenTextSplitter.builder()
            .withChunkSize(CHUNK_SIZE)
            .withMinChunkLengthToEmbed(MIN_CHUNK_LENGTH_TO_EMBED)
            .build();

    @Override
    public @Nullable PostChunks process(Post post) {
        if (post.getContent() == null || post.getContent().isBlank()) {
            return null;
        }

        String plainTextContent = contentExtractor.extractPlainText(post.getContent());
        if (plainTextContent.isBlank()) {
            return null;
        }

        List<Document> source = List.of(Document.builder()
                .text(plainTextContent)
                .build());

        List<Document> chunks = tokenTextSplitter.apply(source);
        if (chunks.isEmpty()) {
            return null;
        }

        String header = header(post);
        chunks = chunks.stream()
                .map(chunk -> chunk.mutate()
                        .text(header + chunk.getText())
                        .metadata("postId", post.getNotionPageId())
                        .build())
                .toList();
        return new PostChunks(post, chunks);
    }

    private String header(Post post) {
        StringBuilder sb = new StringBuilder("# ").append(post.getTitle()).append("\n");
        if (post.getCategory() != null) {
            sb.append("카테고리: ").append(post.getCategory().getName()).append("\n");
        }
        if (!post.getTags().isEmpty()) {
            sb.append("태그: ")
                    .append(post.getTags().stream().map(Tag::getName).collect(Collectors.joining(", ")))
                    .append("\n");
        }
        return sb.append("\n").toString();
    }
}