package junhyeok.blog.acceptance;

import java.time.LocalDateTime;
import junhyeok.blog.domain.PostRepository;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.domain.TagRepository;
import junhyeok.blog.util.PostBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient.ResponseSpec;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
public class TagAcceptanceTest {

    @Autowired
    RestTestClient client;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    PostRepository postRepository;

    @Test
    void 태그_조회_API를_호출한다() {
        Tag tag1 = tagRepository.save(new Tag("tagId1", "태그1", "빨강", 1, LocalDateTime.of(2026, 1, 1, 0, 0)));
        Tag tag2 = tagRepository.save(new Tag("tagId2", "태그2", "파랑", 2, LocalDateTime.of(2026, 1, 1, 0, 0)));
        postRepository.save(PostBuilder.create(1).tags(tag1, tag2).build());

        ResponseSpec response = client.get()
                .uri("/tags")
                .exchange();

        response.expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[0].tagId").isEqualTo("tagId1")
                .jsonPath("$[0].name").isEqualTo("태그1")
                .jsonPath("$[0].color").isEqualTo("빨강")
                .jsonPath("$[0].sortOrder").isEqualTo(1)
                .jsonPath("$[1].tagId").isEqualTo("tagId2")
                .jsonPath("$[1].name").isEqualTo("태그2")
                .jsonPath("$[1].color").isEqualTo("파랑")
                .jsonPath("$[1].sortOrder").isEqualTo(2);
        ;
    }
}
