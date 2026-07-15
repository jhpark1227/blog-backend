package junhyeok.blog.presentation;

import java.util.List;
import junhyeok.blog.application.CategoryService;
import junhyeok.blog.application.dto.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("/categories")
    public List<CategoryResponse> getCategoriesWithPublishedPostCount() {
        return categoryService.getCategoriesWithPublishedPostCount();
    }
}
