package junhyeok.blog.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.parameterWithName;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath;
import static org.springframework.restdocs.snippet.Attributes.key;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.SimpleType;
import jakarta.servlet.http.Cookie;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import junhyeok.blog.application.PostService;
import junhyeok.blog.application.dto.response.PostDetailResponse;
import junhyeok.blog.application.dto.response.PostResponse;
import junhyeok.blog.application.dto.response.PostResponse.CategoryResponse;
import junhyeok.blog.application.dto.response.PostResponse.TagResponse;
import junhyeok.blog.global.exception.CustomException;
import junhyeok.blog.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(PostController.class)
@AutoConfigureRestDocs
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    PostService postService;

    private PostResponse postResponse() {
        return postResponse(false);
    }

    private PostResponse postResponse(boolean pinned) {
        return new PostResponse(
                "postId1",
                "글제목1",
                "발췌문입니다.",
                LocalDate.of(2026, 1, 1),
                new CategoryResponse("categoryId1", "카테고리1", "빨강"),
                List.of(new TagResponse("tagId1", "태그1", "파랑")),
                pinned
        );
    }

    @Test
    @DisplayName("GET /posts - 쿼리 파라미터를 지정하지 않으면 기본값으로 조회한다")
    void 글_목록_조회_API를_기본값으로_호출한다() throws Exception {
        PageRequest pageable = PageRequest.of(0, 5, Sort.by(Direction.DESC, "publishedDate"));
        given(postService.getPosts(null, null, pageable))
                .willReturn(new PageImpl<>(List.of(postResponse()), pageable, 1));

        ResultActions result = mockMvc.perform(get("/posts"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].notionPageId").value("postId1"))
                .andExpect(jsonPath("$.content[0].category.notionOptionId").value("categoryId1"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    @DisplayName("GET /posts")
    void 글_목록_조회_API를_호출한다() throws Exception {
        PageRequest pageable = PageRequest.of(1, 2, Sort.by(Direction.ASC, "publishedDate"));
        given(postService.getPosts("categoryId1", List.of("tagId1"), pageable))
                .willReturn(new PageImpl<>(List.of(postResponse()), pageable, 3));

        ResultActions result = mockMvc.perform(get("/posts")
                .param("categoryId", "categoryId1")
                .param("tagIds", "tagId1")
                .param("page", "1")
                .param("size", "2")
                .param("sort", "PUBLISHED_DATE")
                .param("direction", "ASC"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].notionPageId").value("postId1"))
                .andExpect(jsonPath("$.content[0].category.notionOptionId").value("categoryId1"))
                .andExpect(jsonPath("$.content[0].tags[0].notionOptionId").value("tagId1"))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andDo(document("posts-get", resource(
                        ResourceSnippetParameters.builder()
                                .tag("post")
                                .summary("글 목록 조회")
                                .description("발행된 글을 카테고리와 태그로 필터링해 페이지 단위로 조회한다.")
                                .queryParameters(
                                        parameterWithName("categoryId").description("카테고리 ID (기본값: 전체)")
                                                .optional(),
                                        parameterWithName("tagIds").description("태그 ID 목록 (기본값: 전체)")
                                                .optional(),
                                        parameterWithName("page").description("페이지 번호").type(SimpleType.INTEGER).defaultValue(0)
                                                .optional(),
                                        parameterWithName("size").description("페이지 크기 (최대: 100)").type(SimpleType.INTEGER).defaultValue(5)
                                                .optional(),
                                        parameterWithName("sort").description("정렬 기준").defaultValue("PUBLISHED_DATE")
                                                .attributes(key("enumValues").value(
                                                        Arrays.stream(PostSortField.values()).map(Enum::name).toList()))
                                                .optional(),
                                        parameterWithName("direction").description("정렬 방향").defaultValue("DESC")
                                                .attributes(key("enumValues").value(
                                                        Arrays.stream(Direction.values()).map(Enum::name).toList()))
                                                .optional()
                                )
                                .responseFields(
                                        fieldWithPath("content[].notionPageId").description("글 ID"),
                                        fieldWithPath("content[].title").description("글 제목"),
                                        fieldWithPath("content[].excerpt").type(JsonFieldType.STRING)
                                                .description("발췌").optional(),
                                        fieldWithPath("content[].publishedDate").description("발행일"),
                                        fieldWithPath("content[].category").description("카테고리"),
                                        fieldWithPath("content[].category.notionOptionId").description("카테고리 ID"),
                                        fieldWithPath("content[].category.name").description("카테고리명"),
                                        fieldWithPath("content[].category.color").description("카테고리 색깔"),
                                        fieldWithPath("content[].tags").description("태그 목록"),
                                        fieldWithPath("content[].tags[].notionOptionId").description("태그 ID"),
                                        fieldWithPath("content[].tags[].name").description("태그명"),
                                        fieldWithPath("content[].tags[].color").description("태그 색깔"),
                                        fieldWithPath("content[].pinned").description("고정 여부"),
                                        fieldWithPath("page").description("페이지"),
                                        fieldWithPath("size").description("페이지 사이즈"),
                                        fieldWithPath("totalElements").description("전체 개수"),
                                        fieldWithPath("totalPages").description("전체 페이지수")
                                )
                                .build()
                )));
    }

    @Test
    @DisplayName("GET /posts")
    void 존재하지_않는_정렬기준으로_글_목록_조회_API를_호출하면_400() throws Exception {
        given(postService.getPosts(any(), any(), any()))
                .willReturn(null);

        ResultActions result = mockMvc.perform(get("/posts")
                .param("categoryId", "categoryId1")
                .param("tagIds", "tagId1")
                .param("page", "1")
                .param("size", "2")
                .param("sort", "INVALID_SORT_BY")
                .param("direction", "ASC"));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"))
                .andDo(document("post-get-bad-request", resource(
                        ResourceSnippetParameters.builder()
                                .responseFields(
                                        fieldWithPath("errorCode").description("에러 코드 (잘못된 요청)")
                                )
                                .build()
                )));
    }

    @Test
    @DisplayName("GET /posts/{postId}")
    void 글_단건_조회_API를_호출한다() throws Exception {
        PostDetailResponse postDetailResponse = new PostDetailResponse(
                "id1",
                "제목1",
                "{\"blocks\":[]}",
                LocalDate.of(2026, 1, 1),
                new PostDetailResponse.CategoryResponse("id1", "카테고리1", "빨강"),
                List.of(new PostDetailResponse.TagResponse("id1", "태그1", "파랑"))
        );
        given(postService.getPostAndIncreaseViewCount("id1"))
                .willReturn(postDetailResponse);

        ResultActions result = mockMvc.perform(get("/posts/{postId}", "id1"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.notionPageId").value("id1"))
                .andExpect(jsonPath("$.title").value("제목1"))
                .andExpect(jsonPath("$.content").hasJsonPath())
                .andExpect(jsonPath("$.publishedDate").value("2026-01-01"))
                .andExpect(jsonPath("$.category").isMap())
                .andExpect(jsonPath("$.category.notionOptionId").value("id1"))
                .andExpect(jsonPath("$.category.name").value("카테고리1"))
                .andExpect(jsonPath("$.category.color").value("빨강"))
                .andExpect(jsonPath("$.tags.length()").value(1))
                .andExpect(jsonPath("$.tags[0].notionOptionId").value("id1"))
                .andExpect(jsonPath("$.tags[0].name").value("태그1"))
                .andExpect(jsonPath("$.tags[0].color").value("파랑"))
                .andDo(document("post-get", resource(
                        ResourceSnippetParameters.builder()
                                .tag("post")
                                .summary("글 단건 조회")
                                .description("발행된 글을 한 건 조회한다.")
                                .pathParameters(
                                        parameterWithName("postId").description("글 ID")
                                )
                                .responseFields(
                                        fieldWithPath("notionPageId").description("글 ID"),
                                        fieldWithPath("title").description("글 제목"),
                                        subsectionWithPath("content").description("글 내용 JSON"),
                                        fieldWithPath("publishedDate").description("발행일"),
                                        fieldWithPath("category").description("카테고리"),
                                        fieldWithPath("category.notionOptionId").description("카테고리 ID"),
                                        fieldWithPath("category.name").description("카테고리명"),
                                        fieldWithPath("category.color").description("색깔"),
                                        fieldWithPath("tags").description("태그 목록"),
                                        fieldWithPath("tags[].notionOptionId").description("태그 ID"),
                                        fieldWithPath("tags[].name").description("태그명"),
                                        fieldWithPath("tags[].color").description("태그 색깔")
                                )
                                .build()
                )));
    }

    @Test
    @DisplayName("GET /posts/{postId}")
    void 존재하지_않는_ID로_글_단건_조회_API를_호출하면_404_응답() throws Exception {
        given(postService.getPostAndIncreaseViewCount("notFoundId"))
                .willThrow(new CustomException(ErrorCode.POST_NOT_FOUND));

        ResultActions result = mockMvc.perform(get("/posts/{postId}", "notFoundId"));

        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("POST_NOT_FOUND"))
                .andDo(document("post-get-not-found", resource(
                        ResourceSnippetParameters.builder()
                                .tag("post")
                                .summary("글 단건 조회")
                                .description("발행된 글을 한 건 조회한다.")
                                .pathParameters(
                                        parameterWithName("postId").description("글 ID")
                                )
                                .responseFields(
                                        fieldWithPath("errorCode").description("에러 코드 (존재하지 않는 글)")
                                )
                                .build()
                )));
    }

    @Test
    @DisplayName("GET /posts/{postId}")
    void 쿠키에_해당_글_ID가_있으면_조회수를_올리지_않는다() throws Exception {
        PostDetailResponse postDetailResponse = new PostDetailResponse(
                "id1",
                "제목1",
                "{\"blocks\":[]}",
                LocalDate.of(2026, 1, 1),
                new PostDetailResponse.CategoryResponse("id1", "카테고리1", "빨강"),
                List.of(new PostDetailResponse.TagResponse("id1", "태그1", "파랑"))
        );
        given(postService.getPost("id1"))
                .willReturn(postDetailResponse);

        mockMvc.perform(get("/posts/{postId}", "id1")
                        .cookie(new Cookie("VIEWED-POST-IDS", "id1")))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist(HttpHeaders.SET_COOKIE));

        verify(postService, never()).getPostAndIncreaseViewCount(any());
    }

    @Test
    @DisplayName("GET /posts/{postId}")
    void 쿠키에_해당_글_ID가_없으면_조회수를_올리고_쿠키에_추가한다() throws Exception {
        PostDetailResponse postDetailResponse = new PostDetailResponse(
                "id1",
                "제목1",
                "{\"blocks\":[]}",
                LocalDate.of(2026, 1, 1),
                new PostDetailResponse.CategoryResponse("id1", "카테고리1", "빨강"),
                List.of(new PostDetailResponse.TagResponse("id1", "태그1", "파랑"))
        );
        given(postService.getPostAndIncreaseViewCount("id1"))
                .willReturn(postDetailResponse);

        ResultActions result = mockMvc.perform(get("/posts/{postId}", "id1")
                .cookie(new Cookie("VIEWED-POST-IDS", "id2")));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.notionPageId").value("id1"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("VIEWED-POST-IDS=id2.id1")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")));

        verify(postService, never()).getPost(any());
    }

    @Test
    @DisplayName("GET /posts/pinned")
    void 고정_글_목록_조회_API를_호출한다() throws Exception {
        given(postService.getPinnedPosts())
                .willReturn(List.of(postResponse(true)));

        ResultActions result = mockMvc.perform(get("/posts/pinned"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].notionPageId").value("postId1"))
                .andExpect(jsonPath("$[0].category.notionOptionId").value("categoryId1"))
                .andExpect(jsonPath("$[0].tags[0].notionOptionId").value("tagId1"))
                .andExpect(jsonPath("$[0].pinned").value(true))
                .andDo(document("posts-pinned-get", resource(
                        ResourceSnippetParameters.builder()
                                .tag("post")
                                .summary("고정 글 목록 조회")
                                .description("상단에 고정된 발행 글을 조회한다.")
                                .responseFields(
                                        fieldWithPath("[].notionPageId").description("글 ID"),
                                        fieldWithPath("[].title").description("글 제목"),
                                        fieldWithPath("[].excerpt").type(JsonFieldType.STRING)
                                                .description("발췌").optional(),
                                        fieldWithPath("[].publishedDate").description("발행일"),
                                        fieldWithPath("[].category").description("카테고리"),
                                        fieldWithPath("[].category.notionOptionId").description("카테고리 ID"),
                                        fieldWithPath("[].category.name").description("카테고리명"),
                                        fieldWithPath("[].category.color").description("카테고리 색깔"),
                                        fieldWithPath("[].tags").description("태그 목록"),
                                        fieldWithPath("[].tags[].notionOptionId").description("태그 ID"),
                                        fieldWithPath("[].tags[].name").description("태그명"),
                                        fieldWithPath("[].tags[].color").description("태그 색깔"),
                                        fieldWithPath("[].pinned").description("고정 여부")
                                )
                                .build()
                )));
    }
}