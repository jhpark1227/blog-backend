package junhyeok.blog.application.batch.excerpt;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@StepScope
@RequiredArgsConstructor
public class ExcerptRefreshTargetReader implements ItemReader<Post> {

    private static final int PAGE_SIZE = 50;

    private final PostRepository postRepository;

    private final Deque<Post> buffer = new ArrayDeque<>();
    private String cursor = "";

    @Override
    public @Nullable Post read() {
        if (buffer.isEmpty()) {
            List<Post> page = postRepository.findExcerptRefreshTargetsAfter(cursor, PageRequest.of(0, PAGE_SIZE));
            if (page.isEmpty()) {
                return null;
            }

            buffer.addAll(page);
            cursor = page.getLast().getNotionPageId();
        }

        return buffer.poll();
    }
}
