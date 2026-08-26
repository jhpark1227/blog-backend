package junhyeok.blog.domain;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, String>, PostRepositoryCustom {

    @Modifying
    @Query("UPDATE Post p SET p.status = PostStatus.DELETED WHERE p.syncedAt < :time")
    int markDeletedWhenNotSyncedSince(@Param("time") LocalDateTime time);

    @Query("SELECT p FROM Post p WHERE p.status = PostStatus.PUBLISHED AND p.pinned IS TRUE ORDER BY p.publishedDate DESC")
    List<Post> findPinnedPublishedPosts();

    @Query("SELECT p FROM Post p WHERE p.status = PostStatus.PUBLISHED " +
            "AND (p.excerpt IS NULL OR p.excerpt.generatedAt < p.notionLastEditedTime) " +
            "AND p.notionPageId > :cursor ORDER BY p.notionPageId ASC")
    List<Post> findExcerptRefreshTargetsAfter(@Param("cursor") String cursor, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.viewCount = p.viewCount + 1 WHERE p.notionPageId = :id")
    void increaseViewCountById(@Param("id") String id);
}
