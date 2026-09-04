package junhyeok.blog.presentation;

import junhyeok.blog.application.ChatService;
import junhyeok.blog.application.dto.response.ChatResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chat")
    public ChatResponse chat(@RequestParam("prompt") String prompt) {
        return chatService.chat(prompt);
    }
}
