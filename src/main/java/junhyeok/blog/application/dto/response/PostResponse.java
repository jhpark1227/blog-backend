package junhyeok.blog.application.dto.response;

import java.time.LocalDate;
import java.util.List;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.Tag;

public record PostResponse(
        String notionPageId,
        String title,
        String excerpt,
        LocalDate publishedDate,
        CategoryResponse category,
        List<TagResponse> tags,
        boolean pinned,
        int viewCount
) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getNotionPageId(),
                post.getTitle(),
                post.getExcerpt() != null ? post.getExcerpt().value() : null,
                post.getPublishedDate(),
                post.getCategory() != null ? CategoryResponse.from(post.getCategory()) : null,
                post.getTags().stream()
                        .map(TagResponse::from)
                        .toList(),
                post.isPinned(),
                post.getViewCount()
        );
    }

    public record CategoryResponse(
            String notionOptionId,
            String name,
            String color
    ) {
        private static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getNotionOptionId(), category.getName(), category.getColor());
        }
    }

    public record TagResponse(
            String notionOptionId,
            String name,
            String color
    ) {
        private static TagResponse from(Tag tag) {
            return new TagResponse(tag.getNotionOptionId(), tag.getName(), tag.getColor());
        }
    }
}
