package junhyeok.blog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import junhyeok.blog.application.dto.response.PostDetailResponse;
import junhyeok.blog.application.dto.response.PostDetailResponse.TagResponse;
import junhyeok.blog.application.dto.response.PostResponse;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.CategoryRepository;
import junhyeok.blog.domain.Post;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.domain.PostStatus;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.domain.TagRepository;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import junhyeok.blog.util.PostBuilder;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;

@SpringBootTest
class PostServiceTest {

    @Autowired
    PostService sut;

    @Autowired
    PostRepository postRepository;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        tagRepository.deleteAllInBatch();
    }

    @Test
    void 글_목록을_조회한다() {
        Tag tag1 = tagRepository.save(new Tag("tagId1", "태그1", "빨강", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Tag tag2 = tagRepository.save(new Tag("tagId2", "태그2", "파랑", 2, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Category category1 = categoryRepository.save(new Category("categoryId1", "카테고리1", "빨강", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Category category2 = categoryRepository.save(new Category("categoryId2", "카테고리2", "파랑", 2, LocalDateTime.of(2026, 1, 1, 0, 0)));
        postRepository.save(
                PostBuilder.create(1)
                        .publishedDate(LocalDate.of(2026, 1, 1))
                        .category(category1)
                        .tags(tag1)
                        .build()
        );
        postRepository.save(
                PostBuilder.create(2)
                        .publishedDate(LocalDate.of(2026, 1, 2))
                        .category(category2)
                        .tags(tag1, tag2)
                        .build()
        );
        postRepository.save(
                PostBuilder.create(3)
                        .publishedDate(LocalDate.of(2026, 1, 3))
                        .category(category1)
                        .tags(tag1, tag2)
                        .build()
        );
        postRepository.save(
                PostBuilder.create(4)
                        .publishedDate(LocalDate.of(2026, 1, 4))
                        .category(category2)
                        .tags(tag1, tag2)
                        .build()
        );
        postRepository.save(
                PostBuilder.create(5)
                        .publishedDate(LocalDate.of(2026, 1, 5))
                        .category(category2)
                        .tags(tag2)
                        .build()
        );
        Pageable pageable = PageRequest.of(0, 1, Sort.by(Order.desc("publishedDate")));

        Page<PostResponse> posts = sut.getPosts("categoryId1", List.of("tagId1"), pageable);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(posts).extracting(PostResponse::notionPageId).containsExactly("postId3");
        softly.assertThat(posts.getTotalElements()).isEqualTo(2);
        softly.assertThat(posts.getTotalPages()).isEqualTo(2);
        softly.assertThat(posts.hasNext()).isTrue();
        softly.assertThat(posts.getContent().getFirst())
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(new PostResponse(
                        "postId3",
                        "제목3",
                        null,
                        LocalDate.of(2026, 1, 3),
                        new PostResponse.CategoryResponse("categoryId1", "카테고리1", "빨강"),
                        List.of(
                                new PostResponse.TagResponse("tagId1", "태그1", "빨강"),
                                new PostResponse.TagResponse("tagId2", "태그2", "파랑")
                        ),
                        false,
                        0
                ));
        softly.assertAll();
    }

    @Test
    void 글을_조회한다() {
        Tag tag1 = tagRepository.save(new Tag("tagId1", "태그1", "빨강", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Category category1 = categoryRepository.save(new Category("categoryId1", "카테고리1", "빨강", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Post post = postRepository.save(
                PostBuilder.create(1)
                        .category(category1)
                        .tags(tag1)
                        .build()
        );

        PostDetailResponse findPost = sut.getPost(post.getNotionPageId());

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(findPost.notionPageId()).isEqualTo(post.getNotionPageId());
        softly.assertThat(findPost.title()).isEqualTo(post.getTitle());
        softly.assertThat(findPost.content()).isEqualTo(post.getContent());
        softly.assertThat(findPost.publishedDate()).isEqualTo(post.getPublishedDate());
        softly.assertThat(findPost.tags()).extracting(TagResponse::notionOptionId).containsExactly("tagId1");
        softly.assertThat(findPost.category().notionOptionId()).isEqualTo("categoryId1");
        softly.assertAll();
    }

    @Test
    void 글을_조회하고_조회수를_증가시킨다() {
        Tag tag1 = tagRepository.save(new Tag("tagId1", "태그1", "빨강", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Category category1 = categoryRepository.save(new Category("categoryId1", "카테고리1", "빨강", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Post post = postRepository.save(
                PostBuilder.create(1)
                        .category(category1)
                        .tags(tag1)
                        .build()
        );

        PostDetailResponse findPost = sut.getPostAndIncreaseViewCount(post.getNotionPageId());

        Post savedPost = postRepository.findById(post.getNotionPageId()).orElseThrow(NoSuchElementException::new);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(findPost.notionPageId()).isEqualTo(post.getNotionPageId());
        softly.assertThat(findPost.title()).isEqualTo(post.getTitle());
        softly.assertThat(findPost.content()).isEqualTo(post.getContent());
        softly.assertThat(findPost.publishedDate()).isEqualTo(post.getPublishedDate());
        softly.assertThat(findPost.tags()).extracting(TagResponse::notionOptionId).containsExactly("tagId1");
        softly.assertThat(findPost.category().notionOptionId()).isEqualTo("categoryId1");
        softly.assertThat(savedPost.getViewCount()).isEqualTo(1);
        softly.assertAll();
    }

    @Test
    void 글을_조회하면_조회수가_누적된다() {
        Post post = postRepository.save(
                PostBuilder.create(1)
                        .build()
        );

        for (int i = 0; i < 3; i++) {
            sut.getPostAndIncreaseViewCount(post.getNotionPageId());
        }

        Post savedPost = postRepository.findById(post.getNotionPageId()).orElseThrow();
        assertThat(savedPost.getViewCount()).isEqualTo(3);
    }

    @Test
    void 존재하지_않는_ID로_조회하면_예외가_발생한다() {
        assertThatThrownBy(() -> sut.getPost("notFoundPost"))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.POST_NOT_FOUND);
    }

    @Test
    void 고정된_글_목록을_조회한다() {
        postRepository.save(
                PostBuilder.create(1)
                        .publishedDate(LocalDate.of(2026, 1, 1))
                        .pinned(true)
                        .build()
        );
        postRepository.save(
                PostBuilder.create(2)
                        .publishedDate(LocalDate.of(2026, 1, 2))
                        .pinned(true)
                        .build()
        );
        postRepository.save(
                PostBuilder.create(3)
                        .publishedDate(LocalDate.of(2026, 1, 3))
                        .pinned(true)
                        .status(PostStatus.PENDING)
                        .build()
        );
        postRepository.save(
                PostBuilder.create(4)
                        .publishedDate(LocalDate.of(2026, 1, 4))
                        .pinned(false)
                        .build()
        );

        List<PostResponse> posts = sut.getPinnedPosts();

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(posts).extracting(PostResponse::notionPageId).containsExactly("postId2", "postId1");
        softly.assertThat(posts).allMatch(PostResponse::pinned);
        softly.assertAll();
    }
}