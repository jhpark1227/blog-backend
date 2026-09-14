package junhyeok.blog.application.batch.excerpt;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
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

@Component
@StepScope
@RequiredArgsConstructor
public class ExcerptRefreshTargetReader extends ItemStreamSupport implements ItemReader<Post> {

    private static final String CURSOR_KEY = "cursor";
    private static final int PAGE_SIZE = 50;

    private final PostRepository postRepository;

    private final Deque<String> buffer = new ArrayDeque<>();
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
        if (buffer.isEmpty()) {
            List<String> page = postRepository.findExcerptRefreshTargetIdsAfter(cursor, PageRequest.of(0, PAGE_SIZE));
            if (page.isEmpty()) {
                return null;
            }

            buffer.addAll(page);
        }

        String postId = buffer.poll();
        cursor = postId;
        return postRepository.findById(postId).orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }
}
