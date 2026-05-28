package junhyeok.blog.application.dto.response;

import java.util.List;
import junhyeok.blog.domain.Category;

public record GetCategoryResponse(
        long id,
        String name,
        List<GetCategoryResponse> children
) {
    public static GetCategoryResponse from(Category category) {
        return new GetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getChildren()
                        .stream()
                        .map(GetCategoryResponse::from)
                        .toList()
        );
    }
}
