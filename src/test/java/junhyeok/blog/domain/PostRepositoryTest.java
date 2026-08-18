package junhyeok.blog.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import junhyeok.blog.util.PostBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class PostRepositoryTest {

    @Autowired
    PostRepository sut;

    @Autowired
    TestEntityManager em;

    @Test
    void 특정시간_이전에_갱신된_글을_삭제한다() {
        LocalDateTime standardTime = LocalDateTime.of(2026, 1, 1, 0, 0);
        Post post1 = PostBuilder.create(1)
                .syncedAt(standardTime.minusSeconds(1))
                .build();
        Post post2 = PostBuilder.create(2)
                .syncedAt(standardTime.minusSeconds(1))
                .build();
        Post post3 = PostBuilder.create(3)
                .syncedAt(standardTime)
                .build();
        em.persist(post1);
        em.persist(post2);
        em.persist(post3);
        em.flush();

        int affectedRows = sut.markDeletedWhenNotSyncedSince(standardTime);

        assertThat(affectedRows).isEqualTo(2);
    }

    @Test
    void 고정된_발행_상태의_글을_발행일_내림차순으로_조회한다() {
        Post post1 = PostBuilder.create(1)
                .publishedDate(LocalDate.of(2026, 1, 1))
                .pinned(true)
                .build();
        Post post2 = PostBuilder.create(2)
                .publishedDate(LocalDate.of(2026, 1, 2))
                .pinned(true)
                .build();
        em.persistAndFlush(post1);
        em.persistAndFlush(post2);

        List<Post> posts = sut.findPinnedPublishedPosts();

        assertThat(posts).containsExactly(post2, post1);
    }
}
