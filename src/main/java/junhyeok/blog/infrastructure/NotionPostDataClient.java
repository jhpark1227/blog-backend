package junhyeok.blog.infrastructure;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junhyeok.blog.application.PostDataClient;
import junhyeok.blog.domain.PostData;
import junhyeok.blog.domain.PostDataResult;
import junhyeok.blog.infrastructure.NotionDataSourceResponse.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotionPostDataClient implements PostDataClient {

    public static final int PAGE_SIZE = 10;
    private final String DATA_SOURCE_ID;

    private final RestClient restClient;

    public NotionPostDataClient(
            @Value("${notion.datasource-id}") String dataSourceId,
            @Value("${notion.token}") String token
    ) {
        this.DATA_SOURCE_ID = dataSourceId;
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Notion-Version", "2026-03-11")
                .baseUrl("https://api.notion.com")
                .build();
    }

    @Override
    public PostDataResult get(String cursor) {
        Map<String, Object> requestBody = new HashMap<>();
        if (cursor != null) {
            requestBody.put("start_cursor", cursor);
        }
        requestBody.put("page_size", PAGE_SIZE);
        NotionDataSourceResponse response = restClient.post()
                .uri("/v1/data_sources/{id}/query", DATA_SOURCE_ID)
                .body(requestBody)
                .retrieve()
                .body(NotionDataSourceResponse.class);
        List<PostData> list = response.results()
                .stream()
                .map(PageResult::convertToPostData)
                .toList();
        return new PostDataResult(list, response.nextCursor(), response.hasMore());
    }
}
