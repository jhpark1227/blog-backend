package junhyeok.blog.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, String>, PostRepositoryCustom {

    @Query(value = "SELECT * FROM post WHERE notion_page_id = :notionPageId", nativeQuery = true)
    Optional<Post> findByNotionPageIdIgnoringDelete(@Param("notionPageId") String notionPageId);

    @Modifying
    @Query("UPDATE Post p SET p.status = PostStatus.DELETED WHERE p.syncedAt < :time")
    int markDeletedWhenNotSyncedSince(@Param("time") LocalDateTime time);

    @Query("SELECT t FROM Post p JOIN p.tags t WHERE p.status = 'PUBLISHED' GROUP BY t ORDER BY COUNT(t) DESC")
    List<Tag> findAllTags();

    @Query("SELECT p FROM Post p WHERE p.status = PostStatus.PUBLISHED AND p.pinned IS TRUE ORDER BY p.publishedDate DESC")
    List<Post> findAllPinnedPost();

    @Query("SELECT p FROM Post p WHERE p.status = PostStatus.PUBLISHED " +
            "AND (p.excerpt IS NULL OR p.excerpt.generatedAt < p.notionLastEditedTime) " +
            "AND p.notionPageId > :cursor ORDER BY p.notionPageId ASC")
    List<Post> findExcerptRefreshTargetsAfter(@Param("cursor") String cursor, Pageable pageable);
}
