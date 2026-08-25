package junhyeok.blog.domain;

import java.time.LocalDateTime;
import java.util.List;
import junhyeok.blog.application.dto.response.CategoryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, String> {

    @Modifying
    @Query("DELETE FROM Category c WHERE c.syncedAt < :time")
    int deleteWhenNotSyncedSince(@Param("time") LocalDateTime time);

    @Query("""
            SELECT new junhyeok.blog.application.dto.response.CategoryResponse(c.notionOptionId, c.name, COUNT(p))
            FROM Category c
            LEFT JOIN Post p ON p.category = c AND p.status = PostStatus.PUBLISHED
            GROUP BY c
            ORDER BY c.sortOrder ASC
            """)
    List<CategoryResponse> findAllWithPublishedPostCount();
}
