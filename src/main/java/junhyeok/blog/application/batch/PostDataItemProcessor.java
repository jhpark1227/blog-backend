package junhyeok.blog.application.batch;

import java.util.List;
import junhyeok.blog.application.BlogDataClient;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.CategoryRepository;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostData;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.domain.TagRepository;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostDataItemProcessor implements ItemProcessor<PostData, Post> {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BlogDataClient blogDataClient;

    @Override
    public Post process(PostData item) {
        Category category = categoryRepository.findById(item.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CATEGORY_ID));
        List<Tag> tags = tagRepository.findAllById(item.tagIds());
        return postRepository.findByNotionPageIdIgnoringDelete(item.pageId())
                .map(existing -> {
                    boolean contentChanged = !item.lastEditedTime().isEqual(existing.getNotionLastEditedTime());
                    String content = contentChanged
                            ? blogDataClient.getPageContent(item.pageId())
                            : existing.getContent();
                    existing.update(item.title(), item.status(), content, item.publishedDate(), item.lastEditedTime(), category,
                            tags, item.pinned());
                    existing.sync();
                    return existing;
                })
                .orElseGet(() -> {
                    String content = blogDataClient.getPageContent(item.pageId());
                    return new Post(item.pageId(), item.title(), item.status(), content, item.publishedDate(), item.lastEditedTime(),
                            category, tags, item.pinned());
                });
    }
}