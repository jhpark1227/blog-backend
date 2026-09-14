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
import org.springframework.batch.infrastructure.item.ExecutionContext;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.batch.infrastructure.item.ItemStreamException;
import org.springframework.batch.infrastructure.item.ItemStreamSupport;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@StepScope
@Component
@RequiredArgsConstructor
public class PostEmbeddingTargetReader extends ItemStreamSupport implements ItemReader<Post> {

    private static final String CURSOR_KEY = "cursor";

    private final PostRepository postRepository;
    private final Queue<String> queue = new LinkedList<>();

    private String cursor = "";

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        String key = getExecutionContextKey(CURSOR_KEY);
        if (executionContext.containsKey(key)) {
            this.cursor = executionContext.getString(key);
        }
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        executionContext.putString(getExecutionContextKey(CURSOR_KEY), cursor);
    }

    @Override
    public @Nullable Post read() {
        if (queue.isEmpty()) {
            List<String> postIds = postRepository.findEmbeddingTargetIdsAfter(cursor, PageRequest.of(0, 50));
            if (postIds.isEmpty()) {
                return null;
            }
            queue.addAll(postIds);
        }
        String id = queue.poll();
        cursor = id;
        return postRepository.findById(id).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
