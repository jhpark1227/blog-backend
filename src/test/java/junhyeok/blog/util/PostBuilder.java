package junhyeok.blog.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostStatus;
import junhyeok.blog.domain.Tag;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PostBuilder {

    private String notionPageId;
    private String title;
    private PostStatus status;
    private String content;
    private LocalDate publishedDate;
    private LocalDateTime notionLastEditedTime;
    private LocalDateTime syncedAt;
    boolean pinned;
    private Category category;
    private List<Tag> tags;

    public static PostBuilder create(int index) {
        return new PostBuilder(
                "id" + index,
                "제목" + index,
                PostStatus.PUBLISHED,
                "content",
                LocalDate.of(2026, 1, 1),
                LocalDateTime.of(2026, 1, 1, 20, 0),
                LocalDateTime.of(2026, 1, 1, 20, 0),
                false,
                null,
                List.of()
        );
    }

    public PostBuilder tags(List<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public PostBuilder category(Category category) {
        this.category = category;
        return this;
    }

    public PostBuilder publishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
        return this;
    }

    public PostBuilder pinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    public PostBuilder syncedAt(LocalDateTime syncedAt) {
        this.syncedAt = syncedAt;
        return this;
    }

    public PostBuilder status(PostStatus status) {
        this.status = status;
        return this;
    }

    public Post build() {
        return new Post(
                notionPageId,
                title,
                status,
                content,
                publishedDate,
                notionLastEditedTime,
                syncedAt,
                pinned,
                category,
                tags
        );
    }
}
