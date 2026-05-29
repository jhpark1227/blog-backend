package junhyeok.blog.application.dto.response;

import java.time.LocalDateTime;
import junhyeok.blog.domain.Post;

public record GetPostResponse(
        long id,
        String notionPageId,
        String title,
        LocalDateTime createdAt
) {
    public static GetPostResponse from(Post post) {
        return new GetPostResponse(
                post.getId(),
                post.getNotionPageId(),
                post.getTitle(),
                post.getCreatedAt()
        );
    }
}
