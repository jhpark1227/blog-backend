package junhyeok.blog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.util.PostBuilder;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;

class PostTest {

    @Test
    void 글_정보를_수정한다() {
        Category category1 = new Category("id1", "카테고리1", "red", 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        Category category2 = new Category("id2", "카테고리2", "green", 2, LocalDateTime.of(2026, 1, 1, 0, 0));
        Tag tag1 = new Tag("id1", "태그1", "red", 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        Tag tag2 = new Tag("id2", "태그2", "blue", 2, LocalDateTime.of(2026, 1, 1, 0, 0));
        Post post = PostBuilder.create(1)
                .tags(tag1)
                .category(category1)
                .build();

        post.update(
                "새로운제목",
                PostStatus.PENDING,
                "새로운내용",
                LocalDate.of(2025, 12, 31),
                LocalDateTime.of(2025, 12, 31, 23, 59),
                category2,
                List.of(tag2),
                true
        );

        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(post.getTitle()).isEqualTo("새로운제목");
            soft.assertThat(post.getStatus()).isEqualTo(PostStatus.PENDING);
            soft.assertThat(post.getContent()).isEqualTo("새로운내용");
            soft.assertThat(post.getPublishedDate()).isEqualTo(LocalDate.of(2025, 12, 31));
            soft.assertThat(post.getNotionLastEditedTime()).isEqualTo(LocalDateTime.of(2025, 12, 31, 23, 59));
            soft.assertThat(post.getCategory()).isEqualTo(category2);
            soft.assertThat(post.getTags()).containsExactly(tag2);
            soft.assertThat(post.isPinned()).isTrue();
        });
    }

    @Test
    void 글_요약을_수정한다() {
        Post post = PostBuilder.create(1).build();
        Excerpt newExcerpt = new Excerpt("이 글의 요약.");

        post.updateExcerpt(newExcerpt);

        assertThat(post.getExcerpt()).isEqualTo(newExcerpt);
    }

    @Test
    void 동기화_시각을_갱신한다() {
        Category category = new Category("id1", "카테고리1", "red", 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        Tag tag = new Tag("id1", "태그1", "red", 1, LocalDateTime.of(2026, 1, 1, 0, 0));
        Post post = new Post(
                "id1",
                "제목1",
                PostStatus.PUBLISHED,
                "내용",
                LocalDate.of(2020, 1, 1),
                LocalDateTime.of(2020, 1, 1, 0, 0),
                LocalDateTime.of(2020, 1, 1, 0, 0),
                false,
                category,
                List.of(tag)
        );

        LocalDateTime newSyncedAt = LocalDateTime.of(2020, 1, 1, 1, 0);
        post.markSynced(newSyncedAt);

        assertThat(post.getSyncedAt()).isEqualTo(newSyncedAt);
    }

    @Test
    void 기존시간보다_이전으로_동기화_시각을_갱신하면_예외가_발생한다() {
        Post post = PostBuilder.create(1)
                .syncedAt(LocalDateTime.of(2026, 1, 1, 12, 0))
                .build();

        LocalDateTime newSyncedAt = LocalDateTime.of(2026, 1, 1, 11, 59);
        assertThatThrownBy(() -> post.markSynced(newSyncedAt))
                .isInstanceOf(CustomException.class);
    }
}
