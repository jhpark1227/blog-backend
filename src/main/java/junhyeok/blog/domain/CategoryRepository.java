package junhyeok.blog.domain;

import java.util.List;
import junhyeok.blog.application.dto.response.CategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query("""
            SELECT new junhyeok.blog.application.dto.response.CategoryResponse(c.notionOptionId, c.name, COUNT(p))
            FROM Category c
            LEFT JOIN Post p ON p.category = c AND p.status = PostStatus.PUBLISHED
            GROUP BY c
            ORDER BY c.sortOrder ASC
            """)
    List<CategoryResponse> findAllWithPublishedPostCount();
}
