package junhyeok.blog.application.batch.excerpt;

import junhyeok.blog.application.ExcerptClient;
import junhyeok.blog.domain.Excerpt;
import junhyeok.blog.domain.Post;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExcerptRefreshTargetProcessor implements ItemProcessor<Post, Post> {

    private final ExcerptClient excerptClient;

    @Override
    public Post process(Post post) {
        Excerpt newExcerpt = excerptClient.generateExcerpt(post);
        post.updateExcerpt(newExcerpt);
        return post;
    }
}
