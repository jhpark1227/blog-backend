package junhyeok.blog.application.batch;

import java.util.List;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostData;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.domain.TagData;
import junhyeok.blog.domain.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostItemProcessor implements ItemProcessor<PostData, Post> {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    @Override
    public Post process(PostData item) {
        List<Tag> tags = tagRepository.findAllById(item.tagData().stream().map(TagData::tagId).toList());
        return postRepository.findByNotionPageIdIgnoringDelete(item.pageId())
                .map(existing -> {
                    existing.update(item.title(), item.lastEditedTime(), tags);
                    existing.restore();
                    existing.sync();
                    return existing;
                })
                .orElseGet(() -> new Post(item.pageId(), item.title(), item.createdTime(), item.lastEditedTime(), tags));
    }
}