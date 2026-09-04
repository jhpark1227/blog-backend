package junhyeok.blog.application.batch.dataSync.embedding;

import java.time.LocalDateTime;
import junhyeok.blog.application.batch.dataSync.PostSyncScheduler;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@StepScope
@Component
public class PostVectorWriter implements ItemWriter<PostChunks> {

    private final LocalDateTime runAt;

    private final PostRepository postRepository;
    private final VectorStore vectorStore;

    public PostVectorWriter(
            @Value("#{jobParameters['" + PostSyncScheduler.RUN_AT + "']}") LocalDateTime runAt,
            PostRepository postRepository,
            VectorStore vectorStore
    ) {
        this.runAt = runAt;
        this.postRepository = postRepository;
        this.vectorStore = vectorStore;
    }

    @Override
    public void write(Chunk<? extends PostChunks> chunk) {
        for (PostChunks postChunks : chunk.getItems()) {
            vectorStore.delete("postId == '%s'".formatted(postChunks.post().getNotionPageId()));
            vectorStore.add(postChunks.documents());
            Post post = postChunks.post();
            post.markEmbedded(runAt);
            postRepository.save(post);
        }
    }
}
