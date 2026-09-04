package junhyeok.blog.application.batch.dataSync.embedding;

import java.util.List;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeleteUnpublishedPostVectorsTasklet implements Tasklet {

    private final PostRepository postRepository;
    private final VectorStore vectorStore;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        String cursor = "";
        int deleted = 0;

        while (true) {
            List<Post> posts = postRepository.findVectorPurgeTargetsAfter(cursor, PageRequest.of(0, 50));
            if (posts.isEmpty()) {
                break;
            }

            for (Post post : posts) {
                vectorStore.delete("postId == '%s'".formatted(post.getNotionPageId()));
                post.clearEmbedded();
            }

            cursor = posts.getLast().getNotionPageId();
            deleted += posts.size();
        }
        contribution.incrementWriteCount(deleted);
        return RepeatStatus.FINISHED;
    }
}
