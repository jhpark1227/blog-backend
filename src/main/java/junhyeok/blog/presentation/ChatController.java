package junhyeok.blog.presentation;

import jakarta.validation.Valid;
import junhyeok.blog.application.ChatService;
import junhyeok.blog.application.dto.request.ChatRequest;
import junhyeok.blog.application.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/chat")
    public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
        return chatService.chat(request);
    }
}