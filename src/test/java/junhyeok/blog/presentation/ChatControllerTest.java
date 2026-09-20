package junhyeok.blog.presentation;

import static com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document;
import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import junhyeok.blog.application.ChatService;
import junhyeok.blog.application.dto.request.ChatRequest;
import junhyeok.blog.application.dto.response.ChatResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@WebMvcTest(ChatController.class)
@AutoConfigureRestDocs
class ChatControllerTest {

    private static final String CONVERSATION_ID = "0b5e1f4a-1c2d-4e3f-8a9b-7c6d5e4f3a2b";

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ChatService chatService;

    @Test
    void 대화_ID_없이_챗_API를_호출하면_새_대화_ID를_받는다() throws Exception {
        given(chatService.chat(new ChatRequest(null, "스프링 배치에 대해 알려줘")))
                .willReturn(new ChatResponse(CONVERSATION_ID, "스프링 배치는 청크 단위로 처리합니다."));

        ResultActions result = mockMvc.perform(post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"prompt": "스프링 배치에 대해 알려줘"}
                        """));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.content").value("스프링 배치는 청크 단위로 처리합니다."))
                .andDo(document("chat-post", resource(
                        ResourceSnippetParameters.builder()
                                .tag("chat")
                                .summary("챗 메시지 전송")
                                .description("""
                                        AI 어시스턴트에게 질문한다.
                                        conversationId를 비우면 새 대화가 시작되고 응답으로 발급된 ID를 내려준다.
                                        이어지는 질문에 그 ID를 넣으면 이전 대화 맥락이 유지된다.
                                        """)
                                .requestFields(
                                        fieldWithPath("conversationId").type(JsonFieldType.STRING).optional()
                                                .description("대화 ID (첫 요청에서는 생략)"),
                                        fieldWithPath("prompt").description("질문 (최대 1000자)")
                                )
                                .responseFields(
                                        fieldWithPath("conversationId").description("대화 ID"),
                                        fieldWithPath("content").description("답변")
                                )
                                .build()
                )));
    }

    @Test
    void 대화_ID를_넘기면_그대로_전달한다() throws Exception {
        given(chatService.chat(new ChatRequest(CONVERSATION_ID, "그거 더 설명해줘")))
                .willReturn(new ChatResponse(CONVERSATION_ID, "이어지는 답변"));

        ResultActions result = mockMvc.perform(post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"conversationId": "%s", "prompt": "그거 더 설명해줘"}
                        """.formatted(CONVERSATION_ID)));

        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.conversationId").value(CONVERSATION_ID))
                .andExpect(jsonPath("$.content").value("이어지는 답변"));
    }

    @Test
    void 질문이_비어있으면_400을_반환한다() throws Exception {
        ResultActions result = mockMvc.perform(post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"prompt": "  "}
                        """));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }

    @Test
    void 대화_ID가_UUID_형식이_아니면_400을_반환한다() throws Exception {
        ResultActions result = mockMvc.perform(post("/chat")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"conversationId": "not-a-uuid", "prompt": "안녕"}
                        """));

        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_REQUEST"));
    }
}