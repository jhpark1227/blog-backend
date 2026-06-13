package junhyeok.blog.application;

import java.util.List;
import junhyeok.blog.application.dto.response.CategoryResponse;
import junhyeok.blog.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAllWithCount();
    }
}
