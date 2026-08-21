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
import junhyeok.blog.application.CategoryService;
import junhyeok.blog.application.dto.response.CategoryResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(CategoryController.class)
@AutoConfigureRestDocs
class CategoryControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    CategoryService categoryService;

    @Test
    @DisplayName("GET /categories")
    void 카테고리_목록_조회_API를_호출한다() throws Exception {
        given(categoryService.getCategoriesWithPublishedPostCount())
                .willReturn(List.of(
                        new CategoryResponse("id1", "카테고리1", 3L),
                        new CategoryResponse("id2", "카테고리2", 0L)
                ));

        ResultActions result = mockMvc.perform(get("/categories"));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("id1"))
                .andExpect(jsonPath("$[0].name").value("카테고리1"))
                .andExpect(jsonPath("$[0].postCount").value(3))
                .andExpect(jsonPath("$[1].id").value("id2"))
                .andExpect(jsonPath("$[1].name").value("카테고리2"))
                .andExpect(jsonPath("$[1].postCount").value(0))
                .andDo(document("categories-get", resource(
                        ResourceSnippetParameters.builder()
                                .tag("category")
                                .summary("카테고리 목록 조회")
                                .description("모든 카테고리를 발행된 글 개수와 함께 조회한다.")
                                .responseFields(
                                        fieldWithPath("[].id").description("카테고리 ID"),
                                        fieldWithPath("[].name").description("카테고리 이름"),
                                        fieldWithPath("[].postCount").description("발행된 글 개수")
                                )
                                .build()
                )));
    }
}