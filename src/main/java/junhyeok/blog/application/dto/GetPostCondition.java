package junhyeok.blog.application.dto;

public record GetPostCondition(
        Long categoryId,
        int page,
        int size
) {
}
