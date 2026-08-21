package junhyeok.blog.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostStatus;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.global.config.JpaAuditingConfig;
import junhyeok.blog.util.PostBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

@DataJpaTest
@Import({PostRepositoryImpl.class, JpaAuditingConfig.class})
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PostRepositoryImplTest {

    @Autowired
    PostRepositoryImpl sut;

    @Autowired
    TestEntityManager em;

    @Test
    void 발행_상태의_글을_조회한다() {
        for (int i = 1; i <= 3; i++) {
            em.persist(PostBuilder.create(i).status(PostStatus.PUBLISHED).build());
        }
        em.persist(PostBuilder.create(4).status(PostStatus.PENDING).build());
        em.flush();
        em.clear();

        Page<Post> posts = sut.findPublishedBy(null, null, PageRequest.of(0, 5));

        assertAll(
                () -> assertThat(posts).hasSize(3),
                () -> assertThat(posts.getTotalElements()).isEqualTo(3),
                () -> assertThat(posts).extracting(Post::getNotionPageId).containsExactlyInAnyOrder("postId1", "postId2", "postId3")
        );
    }

    @Test
    void 카테고리로_필터링한다() {
        Category category1 = em.persist(new Category("categoryId1", "카테고리1", "빨강", 1));
        Category category2 = em.persist(new Category("categoryId2", "카테고리2", "파랑", 2));
        for (int i = 1; i <= 3; i++) {
            Post post = PostBuilder.create(i)
                    .category(category1)
                    .build();
            em.persist(post);
        }
        for (int i = 4; i <= 6; i++) {
            Post post = PostBuilder.create(i)
                    .category(category2)
                    .build();
            em.persist(post);
        }
        em.flush();
        em.clear();

        Page<Post> posts = sut.findPublishedBy("categoryId1", null, PageRequest.of(0, 10));

        assertAll(
                () -> assertThat(posts).hasSize(3),
                () -> assertThat(posts.getTotalElements()).isEqualTo(3),
                () -> assertThat(posts).extracting(Post::getNotionPageId)
                        .containsExactlyInAnyOrder("postId1", "postId2", "postId3")
        );
    }

    @Test
    void 태그로_필터링한다() {
        Tag tag1 = em.persist(new Tag("tagId1", "태그1", "빨강", 1));
        Tag tag2 = em.persist(new Tag("tagId2", "태그2", "파랑", 2));
        for (int i = 1; i <= 3; i++) {
            Post post = PostBuilder.create(i)
                    .tags(tag1)
                    .build();
            em.persist(post);
        }
        for (int i = 4; i <= 6; i++) {
            Post post = PostBuilder.create(i)
                    .tags(tag2)
                    .build();
            em.persist(post);
        }
        em.flush();
        em.clear();

        Page<Post> posts = sut.findPublishedBy(null, List.of("tagId1"), PageRequest.of(0, 10));

        assertAll(
                () -> assertThat(posts).hasSize(3),
                () -> assertThat(posts.getTotalElements()).isEqualTo(3),
                () -> assertThat(posts).extracting(Post::getNotionPageId)
                        .containsExactlyInAnyOrder("postId1", "postId2", "postId3")
        );
    }

    @Test
    void 태그_여러개로_필터링한다() {
        Tag tag1 = em.persist(new Tag("tagId1", "태그1", "빨강", 1));
        Tag tag2 = em.persist(new Tag("tagId2", "태그2", "파랑", 2));
        em.persist(
                PostBuilder.create(1)
                        .tags(tag1)
                        .build()
        );
        em.persist(
                PostBuilder.create(2)
                        .tags(tag2)
                        .build()
        );
        em.persist(
                PostBuilder.create(3)
                        .tags(tag1, tag2)
                        .build()
        );
        em.persist(
                PostBuilder.create(4)
                        .build()
        );
        em.flush();
        em.clear();

        Page<Post> posts = sut.findPublishedBy(null, List.of("tagId1", "tagId2"), PageRequest.of(0, 10));

        assertAll(
                () -> assertThat(posts).hasSize(3),
                () -> assertThat(posts.getTotalElements()).isEqualTo(3),
                () -> assertThat(posts).extracting(Post::getNotionPageId)
                        .containsExactlyInAnyOrder("postId1", "postId2", "postId3")
        );
    }

    @Test
    void 발행일_내림차순으로_페이징한다() {
        em.persist(
                PostBuilder.create(1)
                        .publishedDate(LocalDate.of(2026, 1, 1))
                        .build()
        );
        em.persist(
                PostBuilder.create(2)
                        .publishedDate(LocalDate.of(2026, 1, 2))
                        .build()
        );
        em.persist(
                PostBuilder.create(3)
                        .publishedDate(LocalDate.of(2026, 1, 3))
                        .build()
        );
        em.flush();
        em.clear();
        PageRequest pageable = PageRequest.of(0, 2, Sort.by(Direction.DESC, "publishedDate"));

        Page<Post> posts = sut.findPublishedBy(null, null, pageable);

        assertAll(
                () -> assertThat(posts).hasSize(2),
                () -> assertThat(posts.getTotalElements()).isEqualTo(3),
                () -> assertThat(posts.getTotalPages()).isEqualTo(2),
                () -> assertThat(posts).extracting(Post::getNotionPageId)
                        .containsExactly("postId3", "postId2")
        );
    }

    @Test
    void 발행일_오름차순으로_페이징한다() {
        em.persist(
                PostBuilder.create(1)
                        .publishedDate(LocalDate.of(2026, 1, 1))
                        .build()
        );
        em.persist(
                PostBuilder.create(2)
                        .publishedDate(LocalDate.of(2026, 1, 2))
                        .build()
        );
        em.persist(
                PostBuilder.create(3)
                        .publishedDate(LocalDate.of(2026, 1, 3))
                        .build()
        );
        em.flush();
        em.clear();
        PageRequest pageable = PageRequest.of(0, 2, Sort.by(Direction.ASC, "publishedDate"));

        Page<Post> posts = sut.findPublishedBy(null, null, pageable);

        assertAll(
                () -> assertThat(posts).hasSize(2),
                () -> assertThat(posts.getTotalElements()).isEqualTo(3),
                () -> assertThat(posts.getTotalPages()).isEqualTo(2),
                () -> assertThat(posts).extracting(Post::getNotionPageId)
                        .containsExactly("postId1", "postId2")
        );
    }

    @Test
    void 발행일_내림차순_제목_오름차순으로_페이징한다() {
        em.persist(
                PostBuilder.create(1)
                        .publishedDate(LocalDate.of(2026, 1, 1))
                        .build()
        );
        em.persist(
                PostBuilder.create(2)
                        .publishedDate(LocalDate.of(2026, 1, 2))
                        .build()
        );
        em.persist(
                PostBuilder.create(3)
                        .publishedDate(LocalDate.of(2026, 1, 2))
                        .build()
        );
        em.flush();
        em.clear();
        PageRequest pageable = PageRequest.of(0, 2, Sort.by(Order.desc("publishedDate"), Order.asc("title")));

        Page<Post> posts = sut.findPublishedBy(null, null, pageable);

        assertAll(
                () -> assertThat(posts).hasSize(2),
                () -> assertThat(posts.getTotalElements()).isEqualTo(3),
                () -> assertThat(posts.getTotalPages()).isEqualTo(2),
                () -> assertThat(posts).extracting(Post::getNotionPageId)
                        .containsExactly("postId2", "postId3")
        );
    }
}
