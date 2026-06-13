package junhyeok.blog.presentation;

import java.util.List;
import junhyeok.blog.application.TagService;
import junhyeok.blog.application.dto.response.TagResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/tags")
    public List<TagResponse> getTags() {
        return tagService.getTags();
    }
}
