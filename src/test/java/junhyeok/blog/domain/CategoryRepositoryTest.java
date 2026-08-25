package junhyeok.blog.domain;

import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import junhyeok.blog.application.dto.response.CategoryResponse;
import junhyeok.blog.global.config.JpaAuditingConfig;
import junhyeok.blog.util.PostBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    CategoryRepository sut;

    @Autowired
    TestEntityManager em;

    @Test
    void 모든_카테고리와_글_개수를_함께_조회한다() {
        Category category1 = em.persist(new Category("id1", "카테고리1", "red", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Category category2 = em.persist(new Category("id2", "카테고리2", "blue", 2, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Category category3 = em.persist(new Category("id3", "카테고리3", "green", 3, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Post post1 = PostBuilder.create(1)
                .category(category2)
                .build();
        Post post2 = PostBuilder.create(2)
                .category(category3)
                .build();
        Post post3 = PostBuilder.create(3)
                .category(category3)
                .build();
        em.persist(post1);
        em.persist(post2);
        em.persist(post3);
        em.flush();

        List<CategoryResponse> categories = sut.findAllWithPublishedPostCount();

        assertThat(categories).containsExactly(
                new CategoryResponse("id1", "카테고리1", 0L),
                new CategoryResponse("id2", "카테고리2", 1L),
                new CategoryResponse("id3", "카테고리3", 2L)
        );
    }

    @Test
    void 기준_시각보다_먼저_동기화된_카테고리를_삭제한다() {
        LocalDateTime runAt = LocalDateTime.of(2026, 1, 2, 0, 0);
        em.persist(new Category("id1", "카테고리1", "red", 1, runAt));
        em.persist(new Category("id2", "카테고리2", "blue", 2, LocalDateTime.of(2026, 1, 1, 0, 0)));
        em.flush();
        em.clear();

        int deletedCount = sut.deleteWhenNotSyncedSince(runAt);
        em.clear();

        assertThat(deletedCount).isEqualTo(1);
        assertThat(sut.findAll()).extracting(Category::getNotionOptionId).containsExactly("id1");
    }
}
