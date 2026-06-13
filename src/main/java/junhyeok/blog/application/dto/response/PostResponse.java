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
        boolean pinned

) {
    public static PostResponse from(Post post) {
        return new PostResponse(
                post.getNotionPageId(),
                post.getTitle(),
                post.getExcerpt(),
                post.getPublishedDate(),
                CategoryResponse.from(post.getCategory()),
                post.getTags().stream()
                        .map(TagResponse::from)
                        .toList(),
                post.isPinned()
        );
    }

    private record CategoryResponse(
            String notionOptionId,
            String name,
            String color
    ) {
        private static CategoryResponse from(Category category) {
            return new CategoryResponse(category.getNotionOptionId(), category.getName(), category.getColor());
        }
    }

    private record TagResponse(
            String notionOptionId,
            String name,
            String color
    ) {
        private static TagResponse from(Tag tag) {
            return new TagResponse(tag.getNotionOptionId(), tag.getName(), tag.getColor());
        }
    }
}
