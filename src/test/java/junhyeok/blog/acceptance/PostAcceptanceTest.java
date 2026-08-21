package junhyeok.blog.acceptance;

import java.time.LocalDate;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.CategoryRepository;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.domain.TagRepository;
import junhyeok.blog.util.PostBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient.ResponseSpec;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class PostAcceptanceTest {

    @Autowired
    RestTestClient client;

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
    void 기본_파라미터로_글_목록을_조회_API를_호출한다() {
        for (int i = 1; i <= 8; i++) {
            postRepository.save(PostBuilder.create(i)
                    .publishedDate(LocalDate.of(2026, 1, i))
                    .build());
        }

        ResponseSpec response = client.get()
                .uri("/posts")
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(5)
                .jsonPath("$.content.length()").isEqualTo(5)
                .jsonPath("$.totalElements").isEqualTo(8)
                .jsonPath("$.totalPages").isEqualTo(2)
                .jsonPath("$.content[0].notionPageId").isEqualTo("postId8")
                .jsonPath("$.content[4].notionPageId").isEqualTo("postId4");
    }

    @Test
    void 카테고리와_태그로_글_목록_조회_API를_호출한다() {
        Tag tag1 = tagRepository.save(new Tag("tagId1", "태그1", "빨강", 1));
        Tag tag2 = tagRepository.save(new Tag("tagId2", "태그2", "파랑", 2));
        Category category1 = categoryRepository.save(new Category("categoryId1", "카테고리1", "빨강", 1));
        Category category2 = categoryRepository.save(new Category("categoryId2", "카테고리2", "파랑", 2));
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
                        .category(category1)
                        .tags(tag2)
                        .build()
        );

        ResponseSpec response = client.get()
                .uri("/posts?categoryId=categoryId1&tagIds=tagId1,tagId2")
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page").isEqualTo(0)
                .jsonPath("$.size").isEqualTo(5)
                .jsonPath("$.content.length()").isEqualTo(3)
                .jsonPath("$.totalElements").isEqualTo(3)
                .jsonPath("$.totalPages").isEqualTo(1)
                .jsonPath("$.content[0].notionPageId").isEqualTo("postId5")
                .jsonPath("$.content[1].notionPageId").isEqualTo("postId3")
                .jsonPath("$.content[2].notionPageId").isEqualTo("postId1");
    }

    @Test
    void 페이지와_크기를_지정하여_글_목록_조회_API를_호출한다() {
        for (int i = 1; i <= 8; i++) {
            postRepository.save(PostBuilder.create(i)
                    .publishedDate(LocalDate.of(2026, 1, i))
                    .build());
        }

        ResponseSpec response = client.get()
                .uri("/posts?page=1&size=5&sort=PUBLISHED_DATE&direction=ASC")
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.page").isEqualTo(1)
                .jsonPath("$.size").isEqualTo(5)
                .jsonPath("$.content.length()").isEqualTo(3)
                .jsonPath("$.totalElements").isEqualTo(8)
                .jsonPath("$.totalPages").isEqualTo(2)
                .jsonPath("$.content[0].notionPageId").isEqualTo("postId6")
                .jsonPath("$.content[2].notionPageId").isEqualTo("postId8");
    }

    @Test
    void 글_상세_조회_API를_호출한다() {
        postRepository.save(PostBuilder.create(1)
                .publishedDate(LocalDate.of(2026, 1, 1))
                .build());

        ResponseSpec response = client.get()
                .uri("/posts/{postId}", "postId1")
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.notionPageId").isEqualTo("postId1")
                .jsonPath("$.content").isMap();
    }

    @Test
    void 존재하지_않는_ID로_글_상세_조회_API를_호출하면_404() {
        ResponseSpec response = client.get()
                .uri("/posts/{postId}", "notFoundId")
                .exchange();

        response.expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("POST_NOT_FOUND");
    }

    @Test
    void 고정된_글_목록을_조회_API를_호출한다() {
        for (int i = 1; i <= 5; i++) {
            postRepository.save(PostBuilder.create(i)
                    .publishedDate(LocalDate.of(2026, 1, i))
                    .pinned(true)
                    .build());
        }
        for (int i = 6; i <= 8; i++) {
            postRepository.save(PostBuilder.create(i)
                    .publishedDate(LocalDate.of(2026, 1, i))
                    .pinned(false)
                    .build());
        }

        ResponseSpec response = client.get()
                .uri("/posts/pinned")
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(5)
                .jsonPath("$[0].notionPageId").isEqualTo("postId5")
                .jsonPath("$[4].notionPageId").isEqualTo("postId1");
    }
}
