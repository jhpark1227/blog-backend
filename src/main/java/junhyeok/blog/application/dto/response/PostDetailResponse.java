package junhyeok.blog.application.dto.response;

import com.fasterxml.jackson.annotation.JsonRawValue;
import java.time.LocalDate;
import java.util.List;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.Tag;

public record PostDetailResponse(
        String notionPageId,
        String title,
        @JsonRawValue String content,
        LocalDate publishedDate,
        CategoryResponse category,
        List<TagResponse> tags,
        int viewCount
) {
    public static PostDetailResponse from(Post post) {
        return new PostDetailResponse(
                post.getNotionPageId(),
                post.getTitle(),
                post.getContent(),
                post.getPublishedDate(),
                post.getCategory() != null ? CategoryResponse.from(post.getCategory()) : null,
                post.getTags().stream()
                        .map(TagResponse::from)
                        .toList(),
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
        private static TagResponse from(Tag category) {
            return new TagResponse(category.getNotionOptionId(), category.getName(), category.getColor());
        }
    }
}