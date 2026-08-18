package junhyeok.blog.domain;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TagRepository extends JpaRepository<Tag, String> {

    @Query("SELECT t FROM Post p JOIN p.tags t WHERE p.status = 'PUBLISHED' GROUP BY t ORDER BY COUNT(t) DESC")
    List<Tag> findTagsOrderByPublishedPostCountDesc();
}
