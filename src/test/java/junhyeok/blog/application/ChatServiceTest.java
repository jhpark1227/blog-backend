package junhyeok.blog.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.UUID;
import junhyeok.blog.application.dto.request.ChatRequest;
import junhyeok.blog.application.dto.response.ChatResponse;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class ChatServiceTest {

    @Autowired
    ChatService sut;

    @Autowired
    ChatMemoryRepository chatMemoryRepository;

    @MockitoBean
    ChatModel chatModel;

    @BeforeEach
    void setUp() {
        given(chatModel.getOptions()).willReturn(ChatOptions.builder().build());
        given(chatModel.call(any(Prompt.class)))
                .willReturn(new org.springframework.ai.chat.model.ChatResponse(
                        List.of(new Generation(new AssistantMessage("답변")))));
    }

    @AfterEach
    void tearDown() {
        chatMemoryRepository.findConversationIds()
                .forEach(chatMemoryRepository::deleteByConversationId);
    }

    @Test
    void 대화_ID가_없으면_새로_발급한다() {
        ChatResponse response = sut.chat(new ChatRequest(null, "안녕"));

        assertThat(UUID.fromString(response.conversationId())).isNotNull();
    }

    @Test
    void 대화_ID를_넘기면_그대로_유지한다() {
        String conversationId = UUID.randomUUID().toString();

        ChatResponse response = sut.chat(new ChatRequest(conversationId, "안녕"));

        assertThat(response.conversationId()).isEqualTo(conversationId);
    }

    @Test
    void 주고받은_대화를_저장한다() {
        String conversationId = sut.chat(new ChatRequest(null, "첫번째 질문")).conversationId();

        List<Message> savedMessages = chatMemoryRepository.findByConversationId(conversationId);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(savedMessages).extracting(Message::getMessageType)
                .containsExactly(MessageType.USER, MessageType.ASSISTANT);
        softly.assertThat(savedMessages).extracting(Message::getText)
                .containsExactly("첫번째 질문", "답변");
        softly.assertAll();
    }

    @Test
    void 같은_대화_ID로_다시_물으면_이전_대화가_프롬프트에_포함된다() {
        String conversationId = sut.chat(new ChatRequest(null, "첫번째 질문")).conversationId();

        sut.chat(new ChatRequest(conversationId, "두번째 질문"));

        List<Message> messages = capturedPrompts().get(1).getInstructions();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(messages)
                .filteredOn(message -> message.getMessageType() == MessageType.USER)
                .extracting(Message::getText)
                .containsExactly("첫번째 질문", "두번째 질문");
        softly.assertThat(messages)
                .filteredOn(message -> message.getMessageType() == MessageType.ASSISTANT)
                .extracting(Message::getText)
                .containsExactly("답변");
        softly.assertAll();
    }

    @Test
    void 다른_대화_ID의_대화는_프롬프트에_포함되지_않는다() {
        sut.chat(new ChatRequest(null, "첫번째 질문"));

        sut.chat(new ChatRequest(null, "두번째 질문"));

        assertThat(capturedPrompts().get(1).getInstructions())
                .filteredOn(message -> message.getMessageType() == MessageType.USER)
                .extracting(Message::getText)
                .containsExactly("두번째 질문");
    }

    private List<Prompt> capturedPrompts() {
        ArgumentCaptor<Prompt> captor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, times(2)).call(captor.capture());
        return captor.getAllValues();
    }
}