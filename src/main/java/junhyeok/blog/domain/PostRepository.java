package junhyeok.blog.domain;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    @Query(value = "SELECT * FROM post WHERE notion_page_id = :notionPageId", nativeQuery = true)
    Optional<Post> findByNotionPageIdIgnoringDelete(@Param("notionPageId") String notionPageId);

    @Modifying
    @Query(value = "UPDATE post SET deleted = true WHERE synced_at < :time AND deleted = false", nativeQuery = true)
    int softDeleteUnsyncedBefore(@Param("time") LocalDateTime time);
}
