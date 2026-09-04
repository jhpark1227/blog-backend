package junhyeok.blog.application.batch.dataSync.embedding;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@StepScope
@Component
@RequiredArgsConstructor
public class PostEmbeddingTargetReader implements ItemReader<Post> {

    private final PostRepository postRepository;
    private final Queue<String> queue = new LinkedList<>();

    private String cursor = "";

    @Override
    public @Nullable Post read() {
        if (queue.isEmpty()) {
            List<String> postIds = postRepository.findEmbeddingTargetIdsAfter(cursor, PageRequest.of(0, 50));
            if (postIds.isEmpty()) {
                return null;
            }
            queue.addAll(postIds);
            cursor = postIds.getLast();
        }
        return postRepository.findById(queue.poll()).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
