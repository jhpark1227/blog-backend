package junhyeok.blog.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import java.util.List;
import junhyeok.blog.application.TagService;
import junhyeok.blog.application.dto.response.TagResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(TagController.class)
@AutoConfigureRestDocs
class TagControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    TagService tagService;

    @Test
    @DisplayName("GET /tags")
    void 태그_목록_조회_API를_호출한다() throws Exception {
        given(tagService.getTags())
                .willReturn(List.of(
                        new TagResponse("tagId1", "태그1", "빨강", 1),
                        new TagResponse("tagId2", "태그2", "파랑", 2)
                ));

        ResultActions result = mockMvc.perform(get("/tags"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].tagId").value("tagId1"))
                .andExpect(jsonPath("$[0].name").value("태그1"))
                .andExpect(jsonPath("$[0].color").value("빨강"))
                .andExpect(jsonPath("$[0].sortOrder").value(1))
                .andExpect(jsonPath("$[1].tagId").value("tagId2"))
                .andExpect(jsonPath("$[1].name").value("태그2"))
                .andExpect(jsonPath("$[1].color").value("파랑"))
                .andExpect(jsonPath("$[1].sortOrder").value(2))
                .andDo(document("tags-get", resource(
                        ResourceSnippetParameters.builder()
                                .tag("tag")
                                .summary("태그 목록 조회")
                                .description("태그 목록을 조회한다.")
                                .responseFields(
                                        fieldWithPath("[].tagId").description("태그 ID"),
                                        fieldWithPath("[].name").description("태그 이름"),
                                        fieldWithPath("[].color").description("태그 색깔"),
                                        fieldWithPath("[].sortOrder").description("정렬 순서")
                                )
                                .build()
                )));
    }
}