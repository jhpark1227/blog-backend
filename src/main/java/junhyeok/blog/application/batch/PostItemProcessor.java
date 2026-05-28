package junhyeok.blog.application.batch;

import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostData;
import junhyeok.blog.domain.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostItemProcessor implements ItemProcessor<PostData, Post> {

    private final PostRepository postRepository;

    @Override
    public Post process(PostData item) {
        return postRepository.findByNotionPageIdIgnoringDelete(item.pageId())
                .map(existing -> {
                    existing.update(item.title(), item.lastEditedTime());
                    existing.restore();
                    existing.sync();
                    return existing;
                })
                .orElseGet(() -> new Post(item.pageId(), item.title(), item.createdTime(), item.lastEditedTime()));
    }
}