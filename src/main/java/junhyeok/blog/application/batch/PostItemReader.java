package junhyeok.blog.application.batch;

import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.batch.infrastructure.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostItemReader implements ItemReader<Post> {

    private final PostRepository postRepository;

    @Override
    public @Nullable Post read() throws Exception {
        return null;
    }
}
