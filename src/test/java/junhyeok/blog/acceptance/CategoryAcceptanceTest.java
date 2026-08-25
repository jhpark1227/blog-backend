package junhyeok.blog.acceptance;

import java.time.LocalDateTime;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.CategoryRepository;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.domain.PostStatus;
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
class CategoryAcceptanceTest {

    @Autowired
    RestTestClient client;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    PostRepository postRepository;

    @AfterEach
    void tearDown() {
        postRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
    }

    @Test
    void 카테고리별_발행된_글_개수를_조회한다() {
        Category category1 = categoryRepository.save(new Category("id1", "카테고리1", "red", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Category category2 = categoryRepository.save(new Category("id2", "카테고리2", "blue", 2, LocalDateTime.of(2026, 1, 1, 0, 0)));
        postRepository.save(PostBuilder.create(1).category(category1).build());
        postRepository.save(PostBuilder.create(2).category(category1).build());
        postRepository.save(PostBuilder.create(3).category(category2).status(PostStatus.PENDING).build());

        ResponseSpec response = client.get()
                .uri("/categories")
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo("id1")
                .jsonPath("$[0].name").isEqualTo("카테고리1")
                .jsonPath("$[0].postCount").isEqualTo(2)
                .jsonPath("$[1].id").isEqualTo("id2")
                .jsonPath("$[1].name").isEqualTo("카테고리2")
                .jsonPath("$[1].postCount").isEqualTo(0);
    }
}