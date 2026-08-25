package junhyeok.blog.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagRepository extends JpaRepository<Tag, String> {

    @Modifying
    @Query("DELETE FROM Tag t WHERE t.syncedAt < :time")
    int deleteWhenNotSyncedSince(@Param("time") LocalDateTime time);

    @Query("""
                SELECT t
                FROM Post p
                JOIN p.tags t
                WHERE p.status = PostStatus.PUBLISHED
                GROUP BY t
                ORDER BY COUNT(t) DESC, t.sortOrder ASC
            """)
    List<Tag> findTagsOrderByPublishedPostCountDesc();
}
