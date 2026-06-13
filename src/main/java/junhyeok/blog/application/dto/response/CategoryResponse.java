package junhyeok.blog.application.dto.response;

public record CategoryResponse(
        String id,
        String name,
        long postCount
) {
}
