package junhyeok.blog.domain;

import java.util.List;
import junhyeok.blog.application.dto.response.CategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query("""
            SELECT new junhyeok.blog.application.dto.response.CategoryResponse(p.category.notionOptionId, p.category.name, COUNT(p.category))
            FROM Post p
            WHERE p.status = PostStatus.PUBLISHED
            GROUP BY p.category
            ORDER BY p.category.sortOrder ASC
            """)
    List<CategoryResponse> findAllWithCount();
}
