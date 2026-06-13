package junhyeok.blog.application.batch;

import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostItemWriter implements ItemWriter<Post> {

    private final PostRepository postRepository;

    @Override
    public void write(Chunk<? extends Post> chunk) throws Exception {
        postRepository.saveAll(chunk.getItems());
    }
}