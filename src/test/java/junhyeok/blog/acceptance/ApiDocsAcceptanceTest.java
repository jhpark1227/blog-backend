package junhyeok.blog.acceptance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.test.web.servlet.client.RestTestClient.ResponseSpec;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
class ApiDocsAcceptanceTest {

    @Autowired
    RestTestClient client;

    @Test
    void 문서_페이지가_제공된다() {
        ResponseSpec response = client.get()
                .uri("/docs.html")
                .exchange();

        response.expectStatus().isOk();
    }
}