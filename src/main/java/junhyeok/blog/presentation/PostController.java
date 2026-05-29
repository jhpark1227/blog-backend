package junhyeok.blog.presentation;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import junhyeok.blog.application.PostService;
import junhyeok.blog.application.dto.response.GetPostResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/posts")
    public Page<GetPostResponse> getPosts(
            @RequestParam @PositiveOrZero int page,
            @RequestParam @Positive @Max(100) int size
    ) {
        return postService.getPosts(page, size);
    }
}
