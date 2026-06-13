package junhyeok.blog.infrastructure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import junhyeok.blog.application.BlogDataClient;
import junhyeok.blog.domain.Category;
import junhyeok.blog.domain.PostData;
import junhyeok.blog.domain.PostDataResult;
import junhyeok.blog.domain.Tag;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import junhyeok.blog.infrastructure.NotionDataSourceQueryResponse.PageResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class NotionBlogDataClient implements BlogDataClient {

    public static final int PAGE_SIZE = 10;
    private final String DATA_SOURCE_ID;

    private final RestClient restClient;
    private final RestClient publicRestClient;
    private final ObjectMapper objectMapper;

    public NotionBlogDataClient(
            @Value("${notion.datasource-id}") String dataSourceId,
            @Value("${notion.token}") String token,
            ObjectMapper objectMapper
    ) {
        this.DATA_SOURCE_ID = dataSourceId;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .defaultHeader("Authorization", "Bearer " + token)
                .defaultHeader("Notion-Version", "2026-03-11")
                .baseUrl("https://api.notion.com")
                .build();
        this.publicRestClient = RestClient.builder()
                .baseUrl("https://www.notion.so")
                .build();
    }

    @Override
    public PostDataResult getPostData(String cursor) {
        Map<String, Object> requestBody = new HashMap<>();
        if (cursor != null) {
            requestBody.put("start_cursor", cursor);
        }
        requestBody.put("page_size", PAGE_SIZE);
        NotionDataSourceQueryResponse response = restClient.post()
                .uri("/v1/data_sources/{id}/query", DATA_SOURCE_ID)
                .body(requestBody)
                .retrieve()
                .body(NotionDataSourceQueryResponse.class);
        List<PostData> list = response.results()
                .stream()
                .map(PageResult::convertToPostData)
                .toList();
        return new PostDataResult(list, response.nextCursor(), response.hasMore());
    }

    @Override
    public List<Category> getCategories() {
        NotionDatasourceResponse response = restClient.get()
                .uri("/v1/data_sources/{id}", DATA_SOURCE_ID)
                .retrieve()
                .body(NotionDatasourceResponse.class);
        return response.getCategories();
    }

    @Override
    public List<Tag> getTags() {
        NotionDatasourceResponse response = restClient.get()
                .uri("/v1/data_sources/{id}", DATA_SOURCE_ID)
                .retrieve()
                .body(NotionDatasourceResponse.class);
        return response.getTags();
    }

    @Override
    public String getPageContent(String pageId) {
        Map<String, Object> requestBody = Map.of(
                "pageId", pageId,
                "limit", 100,
                "cursor", Map.of("stack", List.of()),
                "chunkNumber", 0,
                "verticalColumns", false
        );
        String responseBody = publicRestClient.post()
                .uri("/api/v3/loadPageChunk")
                .body(requestBody)
                .retrieve()
                .body(String.class);
        try {
            ObjectNode recordMap = (ObjectNode) objectMapper.readTree(responseBody).get("recordMap");
            if (recordMap == null) {
                throw new CustomException(ErrorCode.NOTION_RESPONSE_INVALID);
            }
            ObjectNode blockMap = (ObjectNode) recordMap.get("block");
            fetchMissingBlocks(blockMap);
            fetchCollectionData(recordMap, blockMap);
            return transformRecordMap(recordMap);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException(ErrorCode.NOTION_RESPONSE_INVALID);
        }
    }

    private void fetchMissingBlocks(ObjectNode blockMap) {
        while (true) {
            Set<String> missing = findMissingChildIds(blockMap);
            if (missing.isEmpty()) {
                break;
            }

            List<Map<String, Object>> requests = missing.stream()
                    .map(id -> Map.<String, Object>of("pointer", Map.of("table", "block", "id", id), "version", -1))
                    .toList();

            JsonNode response = objectMapper.valueToTree(
                    publicRestClient.post()
                            .uri("/api/v3/syncRecordValues")
                            .body(Map.of("requests", requests))
                            .retrieve()
                            .body(Map.class)
            );

            JsonNode fetchedBlocks = response.path("recordMap").path("block");
            for (String id : fetchedBlocks.propertyNames()) {
                blockMap.set(id, fetchedBlocks.get(id));
            }
        }
    }

    private Set<String> findMissingChildIds(ObjectNode blockMap) {
        Set<String> present = new HashSet<>(blockMap.propertyNames());
        Set<String> missing = new HashSet<>();

        for (String blockId : present) {
            JsonNode content = blockMap.path(blockId).path("value").path("value").path("content");
            if (content.isArray()) {
                for (JsonNode child : content) {
                    String childId = child.asText();
                    if (!present.contains(childId)) {
                        missing.add(childId);
                    }
                }
            }
        }
        return missing;
    }

    private void fetchCollectionData(ObjectNode recordMap, ObjectNode blockMap) {
        for (String blockId : new ArrayList<>(blockMap.propertyNames())) {
            JsonNode blockValue = blockMap.path(blockId).path("value").path("value");
            String type = blockValue.path("type").asText();
            if (!type.equals("collection_view") && !type.equals("collection_view_page")) {
                continue;
            }

            String collectionId = blockValue.path("collection_id").asText();
            String spaceId = blockValue.path("space_id").asText();
            JsonNode viewIdsNode = blockValue.path("view_ids");

            List<Map<String, Object>> requests = new ArrayList<>();
            requests.add(Map.of("pointer", Map.of("table", "collection", "id", collectionId), "version", -1));

            List<String> viewIds = new ArrayList<>();
            if (viewIdsNode.isArray()) {
                for (JsonNode vid : viewIdsNode) {
                    String viewId = vid.asText();
                    viewIds.add(viewId);
                    requests.add(Map.of("pointer", Map.of("table", "collection_view", "id", viewId), "version", -1));
                }
            }

            JsonNode syncResponse = objectMapper.valueToTree(
                    publicRestClient.post()
                            .uri("/api/v3/syncRecordValues")
                            .body(Map.of("requests", requests))
                            .retrieve()
                            .body(Map.class)
            );
            mergeInto(recordMap, "collection", syncResponse.path("recordMap").path("collection"));
            mergeInto(recordMap, "collection_view", syncResponse.path("recordMap").path("collection_view"));

            for (String viewId : viewIds) {
                Map<String, Object> queryBody = Map.of(
                        "collection", Map.of("id", collectionId, "spaceId", spaceId),
                        "collectionView", Map.of("id", viewId, "spaceId", spaceId),
                        "loader", Map.of(
                                "type", "reducer",
                                "reducers", Map.of("collection_group_results", Map.of("type", "results", "limit", 50)),
                                "searchQuery", "",
                                "userTimeZone", "Asia/Seoul"
                        )
                );
                JsonNode queryResponse = objectMapper.valueToTree(
                        publicRestClient.post()
                                .uri("/api/v3/queryCollection")
                                .body(queryBody)
                                .retrieve()
                                .body(Map.class)
                );
                mergeInto(recordMap, "block", queryResponse.path("recordMap").path("block"));

                JsonNode groupResults = queryResponse.path("result")
                        .path("reducerResults")
                        .path("collection_group_results");

                ObjectNode viewResult = objectMapper.createObjectNode();
                viewResult.set("blockIds", groupResults.path("blockIds"));
                viewResult.put("total", groupResults.path("blockIds").size());

                JsonNode collectionQuery = recordMap.get("collection_query");
                ObjectNode collectionQueryMap = (collectionQuery != null)
                        ? (ObjectNode) collectionQuery
                        : objectMapper.createObjectNode();
                JsonNode collectionEntry = collectionQueryMap.get(collectionId);
                ObjectNode collectionEntryMap = (collectionEntry != null)
                        ? (ObjectNode) collectionEntry
                        : objectMapper.createObjectNode();
                collectionEntryMap.set(viewId, viewResult);
                collectionQueryMap.set(collectionId, collectionEntryMap);
                recordMap.set("collection_query", collectionQueryMap);
            }
        }
    }

    private void mergeInto(ObjectNode recordMap, String key, JsonNode source) {
        if (source.isMissingNode() || source.isNull()) {
            return;
        }
        JsonNode existing = recordMap.get(key);
        ObjectNode target = (existing != null) ? (ObjectNode) existing : objectMapper.createObjectNode();
        for (String id : source.propertyNames()) {
            target.set(id, source.get(id));
        }
        recordMap.set(key, target);
    }

    private String transformRecordMap(JsonNode recordMap) {
        ObjectNode result = objectMapper.createObjectNode();

        for (String mapKey : List.of("block", "collection", "collection_view")) {
            JsonNode map = recordMap.get(mapKey);
            if (map == null) {
                continue;
            }

            ObjectNode transformedMap = objectMapper.createObjectNode();
            for (String key : map.propertyNames()) {
                JsonNode item = map.get(key);
                ObjectNode transformedItem = objectMapper.createObjectNode();
                JsonNode valueWrapper = item.get("value");
                if (valueWrapper != null) {
                    transformedItem.set("role", valueWrapper.get("role"));
                    transformedItem.set("value", valueWrapper.get("value"));
                }
                transformedMap.set(key, transformedItem);
            }

            result.set(mapKey, transformedMap);
        }

        if (recordMap.has("collection_query")) {
            result.set("collection_query", recordMap.get("collection_query"));
        }

        List<String> excludedKeys = List.of("block", "collection", "collection_view", "collection_query", "__version__");
        for (String key : recordMap.propertyNames()) {
            if (!excludedKeys.contains(key)) {
                result.set(key, recordMap.get(key));
            }
        }

        return result.toString();
    }
}
