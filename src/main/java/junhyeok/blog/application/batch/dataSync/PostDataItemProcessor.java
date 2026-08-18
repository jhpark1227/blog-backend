package junhyeok.blog.application.batch.dataSync;

import java.time.LocalDateTime;
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
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class PostDataItemProcessor implements ItemProcessor<PostData, Post> {

    private final LocalDateTime runAt;

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BlogDataClient blogDataClient;

    public PostDataItemProcessor(
            @Value("#{jobParameters['" + PostSyncScheduler.RUN_AT + "']}") LocalDateTime runAt,
            PostRepository postRepository,
            CategoryRepository categoryRepository,
            TagRepository tagRepository,
            BlogDataClient blogDataClient
    ) {
        this.runAt = runAt;
        this.postRepository = postRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
        this.blogDataClient = blogDataClient;
    }

    @Override
    public Post process(PostData item) {
        Category category = categoryRepository.findById(item.categoryId())
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_CATEGORY_ID));
        List<Tag> tags = tagRepository.findAllById(item.tagIds());
        return postRepository.findById(item.pageId())
                .map(existing -> {
                    boolean contentChanged = !item.lastEditedTime().isEqual(existing.getNotionLastEditedTime());
                    String content = contentChanged
                            ? blogDataClient.getPageContent(item.pageId())
                            : existing.getContent();
                    existing.update(item.title(), item.status(), content, item.publishedDate(), item.lastEditedTime(), category,
                            tags, item.pinned());
                    existing.markSynced(runAt);
                    return existing;
                })
                .orElseGet(() -> {
                    String content = blogDataClient.getPageContent(item.pageId());
                    return new Post(item.pageId(), item.title(), item.status(), content, item.publishedDate(), item.lastEditedTime(), runAt,
                            item.pinned(), category, tags);
                });
    }
}